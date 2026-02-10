package org.umc.travlocksserver.domain.auth.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.auth.dto.response.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthVerifyEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.repository.EmailVerificationRedisRepository;
import org.umc.travlocksserver.domain.auth.repository.SignupTokenRedisRepository;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.global.common.MailTemplateLoader;
import org.umc.travlocksserver.global.mail.MailSender;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;

import static org.umc.travlocksserver.domain.auth.code.AuthErrorCode.EMAIL_SEND_FAILED;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

	private static final Duration TTL = Duration.ofMinutes(5); // 인증 코드 유효 시간: 5분
	private static final Duration SIGNUP_TOKEN_TTL = Duration.ofMinutes(20);

	private final EmailVerificationRedisRepository redisRepository;
    private final MailSender mailSender;
    private final MailTemplateLoader templateLoader;
    private final MemberRepository memberRepository;
	private final SignupTokenRedisRepository signupTokenRedisRepository;

	private static final SecureRandom RANDOM = new SecureRandom();

	public AuthSendEmailResponseDTO sendVerificationCode(String email) {
		// 이미 가입된 이메일이면 차단
		if (memberRepository.existsByEmail(email)) {
			throw new AuthException(AuthErrorCode.EMAIL_ALREADY_REGISTERED);
		}

		String code = generate6DigitCode();
		String verificationId = generateVerificationId();

		// Redis 저장
		redisRepository.save(
			verificationId,
			new EmailVerificationRedisRepository.EmailVerificationCache(email, code),
			TTL);

        String subject = "[트래블록스] 이메일 인증 코드 안내";
        String html = templateLoader.load("mail/verification-code.html")
                .replace("{{CODE}}", code);
        try {
            mailSender.send(email, subject, html);
        } catch (Exception e) {
            e.printStackTrace();   // ← 이거 하나만으로도 원인 다 나옴
            throw new RuntimeException("EMAIL_SEND_FAILED");
        }

		return new AuthSendEmailResponseDTO(verificationId);
	}

	public AuthVerifyEmailResponseDTO confirmVerificationCode(String verificationId, String code) {

		EmailVerificationRedisRepository.EmailVerificationCache cache = redisRepository.find(verificationId);

		// 만료/없음
		if (cache == null) {
			throw new AuthException(AuthErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
		}

		// 코드 불일치
		if (!cache.code().equals(code)) {
			throw new AuthException(AuthErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
		}

		// 성공 -> signupToken 발급
		String signupToken = generateSignupToken();

		signupTokenRedisRepository.save(signupToken, cache.email(), SIGNUP_TOKEN_TTL);

		// verificationId는 재사용 방지 위해 삭제
		redisRepository.delete(verificationId);

		return new AuthVerifyEmailResponseDTO(signupToken);
	}

	public void resendVerificationCode(String verificationId) {

		// 기존 인증 요청 조회
		EmailVerificationRedisRepository.EmailVerificationCache oldCache = redisRepository.find(verificationId);

		if (oldCache == null) {
			throw new AuthException(AuthErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
		}

		String newCode = generate6DigitCode();


        String subject = "[트래블록스] 이메일 인증 코드 안내";
        String htmlBody = templateLoader.load("mail/verification-code.html")
                .replace("{{CODE}}", newCode);
		try {
            mailSender.send(oldCache.email(), subject, htmlBody);
		} catch (Exception e) {
            // 실패하면 Redis 그대로 유지
			throw new AuthException(EMAIL_SEND_FAILED);
		}

		// 성공했을 때만 Redis 업데이트 (같은 verificationId로 갱신)
		redisRepository.save(
			verificationId,
			new EmailVerificationRedisRepository.EmailVerificationCache(oldCache.email(), newCode),
			TTL);
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

	private String generateSignupToken() {
		byte[] bytes = new byte[16];
		RANDOM.nextBytes(bytes);
		return "signup_" + HexFormat.of().formatHex(bytes); // signup_ + 32hex
	}
}
