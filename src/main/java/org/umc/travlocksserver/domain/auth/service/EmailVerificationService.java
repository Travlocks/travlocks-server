package org.umc.travlocksserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.auth.dto.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.AuthVerifyEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.exception.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.repository.EmailVerificationRedisRepository;
import org.umc.travlocksserver.domain.auth.repository.SignupTokenRedisRepository;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration TTL = Duration.ofMinutes(5); // 인증 코드 유효 시간: 5분
    private static final Duration SIGNUP_TOKEN_TTL = Duration.ofMinutes(20);

    private final EmailVerificationRedisRepository redisRepository;
    private final SesEmailSender emailSender;
    private final MemberRepository memberRepository;
    private final SignupTokenRedisRepository signupTokenRedisRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthSendEmailResponseDTO sendVerificationCode(String email) {
        // 이미 가입된 이메일이면 차단
        if (memberRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        String code = generate6DigitCode(); // 6자리 숫자 코드
        String verificationId = generateVerificationId();

        // Redis 저장
        redisRepository.save(
                verificationId,
                new EmailVerificationRedisRepository.EmailVerificationCache(email, code),
                TTL
        );

        // SesEmailSender 호출 (이메일 발송)
        try {
            emailSender.sendVerificationCode(email, code);
        } catch (Exception e) {
            // SES 발송 실패 시 Redis 롤백
            redisRepository.delete(verificationId);
            throw new AuthException(AuthErrorCode.EMAIL_SEND_FAILED);
        }

        return new AuthSendEmailResponseDTO(verificationId);
    }

    public AuthVerifyEmailResponseDTO confirmVerificationCode(String verificationId, String code) {

        EmailVerificationRedisRepository.EmailVerificationCache cache =
                redisRepository.find(verificationId);

        // 만료/없음
        if (cache == null) {
            throw new AuthException(AuthErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
        }

        // 코드 불일치
        if (!cache.code().equals(code)) {
            throw new AuthException(AuthErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        // 성공 -> signupToken 발급 (1회성)
        String signupToken = generateSignupToken();

        // signupToken -> email 저장 (회원가입 때 사용)
        signupTokenRedisRepository.save(signupToken, cache.email(), SIGNUP_TOKEN_TTL);

        // verificationId는 재사용 방지 위해 삭제
        redisRepository.delete(verificationId);

        return new AuthVerifyEmailResponseDTO(signupToken);
    }

    private String generateSignupToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return "signup_" + HexFormat.of().formatHex(bytes); // signup_ + 32hex
    }

    private String generate6DigitCode() {
        int n = RANDOM.nextInt(900_000) + 100_000; // 100000~999999
        return String.valueOf(n);
    }

    private String generateVerificationId() {
        byte[] bytes = new byte[8];
        RANDOM.nextBytes(bytes);
        return "verif_" + HexFormat.of().formatHex(bytes); // verif_ + 16hex
    }
}
