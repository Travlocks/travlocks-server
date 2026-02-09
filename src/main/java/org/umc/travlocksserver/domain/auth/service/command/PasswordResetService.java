package org.umc.travlocksserver.domain.auth.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.auth.dto.request.AuthPasswordResetConfirmRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthPasswordResetVerifyResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.repository.PasswordResetTokenRedisRepository;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.global.mail.ResendMailSender;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRedisRepository passwordResetTokenRedisRepository;
    private final ResendMailSender resendMailSender;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom RANDOM = new SecureRandom();

    // 최소 8자 + 영문 포함 + 숫자 포함
    private static final Pattern PASSWORD_RULE = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    @Value("${password-reset.redirect-uri}")
    private String redirectUri;

    @Value("${password-reset.ttl-minutes:30}")
    private long ttlMinutes;

    public void sendPasswordResetLink(String email) {
        boolean exists = memberRepository.existsByEmail(email);

        if (!exists) {
            return;
        }

        // resetToken 생성 + Redis 저장
        String resetToken = generateResetToken();
        passwordResetTokenRedisRepository.save(
                resetToken,
                new PasswordResetTokenRedisRepository.PasswordResetCache(email),
                Duration.ofMinutes(ttlMinutes)
        );

        // resetUrl 생성 (token을 query로)
        // 프론트 라우트 예: https://travlocks.kro.kr/reset-password?token=...
        String resetUrl = buildResetUrl(resetToken);

        try {
            resendMailSender.sendPasswordResetLink(email, resetUrl, ttlMinutes);
        } catch (Exception e) {
            passwordResetTokenRedisRepository.delete(resetToken);
            throw e;
        }
    }

    public AuthPasswordResetVerifyResponseDTO verifyResetToken(String token) {
        if (passwordResetTokenRedisRepository.find(token) == null) {
            throw new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        return new AuthPasswordResetVerifyResponseDTO(true);
    }

    @Transactional
    public void confirmPasswordReset(AuthPasswordResetConfirmRequestDTO request) {
        String token = request.token();
        String newPassword = request.newPassword();
        String newPasswordConfirm = request.newPasswordConfirm();

        if (!newPassword.equals(newPasswordConfirm)) {
            throw new AuthException(AuthErrorCode.PASSWORD_RESET_PASSWORD_MISMATCH);
        }

        if (!PASSWORD_RULE.matcher(newPassword).matches()) {
            throw new AuthException(AuthErrorCode.PASSWORD_RESET_WEAK_PASSWORD);
        }

        var cache = passwordResetTokenRedisRepository.find(token);
        if (cache == null) {
            throw new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        String email = cache.email();
        var member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        String encoded = passwordEncoder.encode(newPassword);
        member.changePassword(encoded);

        passwordResetTokenRedisRepository.delete(token);
    }

    private String buildResetUrl(String token) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        if (redirectUri.contains("?")) {
            return redirectUri + "&token=" + encodedToken;
        }
        return redirectUri + "?token=" + encodedToken;
    }

    private String generateResetToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return "pwdreset_" + HexFormat.of().formatHex(bytes);
    }
}
