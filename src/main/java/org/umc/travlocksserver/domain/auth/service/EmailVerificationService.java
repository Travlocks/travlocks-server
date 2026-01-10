package org.umc.travlocksserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.auth.dto.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.exception.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.repository.EmailVerificationRedisRepository;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration TTL = Duration.ofMinutes(5); // 인증 코드 유효 시간: 5분

    private final EmailVerificationRedisRepository redisRepository;
    private final SesEmailSender emailSender;
    private final MemberRepository memberRepository;

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
        emailSender.sendVerificationCode(email, code);

        return new AuthSendEmailResponseDTO(verificationId);
    }

    private String generate6DigitCode() {
        SecureRandom random = new SecureRandom();
        int n = random.nextInt(900_000) + 100_000; // 100000~999999
        return String.valueOf(n);
    }

    private String generateVerificationId() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        return "verif_" + HexFormat.of().formatHex(bytes); // verif_ + 16hex
    }
}
