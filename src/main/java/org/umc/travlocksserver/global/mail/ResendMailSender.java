package org.umc.travlocksserver.global.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.umc.travlocksserver.global.common.MailTemplateLoader;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ResendMailSender implements MailSender {

	private static final String RESEND_API_URL = "https://api.resend.com/emails";

	private final MailTemplateLoader templateLoader;
	private final RestClient restClient = RestClient.create();

	@Value("${mail.resend.api-key}")
	private String apiKey;

	@Value("${mail.resend.from-email}")
	private String fromEmail;

	@Override
	public void send(String toEmail, String subject, String htmlBody) {
		Map<String, Object> body = Map.of(
			"from", fromEmail,
			"to", toEmail,
			"subject", subject,
			"html", htmlBody);

		restClient.post()
			.uri(RESEND_API_URL)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + apiKey)
			.body(body)
			.retrieve()
			.toBodilessEntity();
	}

	// 인증 코드 전용
	public void sendVerificationCode(String toEmail, String code) {
		String subject = "[트래블록스] 이메일 인증 코드 안내";

		String template = templateLoader.load("mail/verification-code.html");
		String htmlBody = template.replace("{{CODE}}", code);

		send(toEmail, subject, htmlBody);
	}

	// 비밀번호 재설정 링크 전용
	public void sendPasswordResetLink(String toEmail, String resetUrl, long ttlMinutes) {
		String subject = "[트래블록스] 비밀번호 재설정 안내";

		String template = templateLoader.load("mail/password-reset-link.html");
		String htmlBody = template
			.replace("{{RESET_URL}}", resetUrl)
			.replace("{{TTL_MINUTES}}", String.valueOf(ttlMinutes));

		send(toEmail, subject, htmlBody);
	}
}
