package org.umc.travlocksserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.umc.travlocksserver.global.common.MailTemplateLoader;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Component
@RequiredArgsConstructor
public class SesEmailSender {

	private final SesClient sesClient; // AWS SES와 통신하는 클라이언트
	private final MailTemplateLoader templateLoader;

	@Value("${aws.ses.from-email}")
	private String fromEmail;

	public void sendVerificationCode(String toEmail, String code) {
		String subject = "[트래블록스] 이메일 인증 코드 안내"; // 메일 제목

		String template = templateLoader.load("mail/verification-code.html");
		String htmlBody = template.replace("{{CODE}}", code);

		SendEmailRequest request = SendEmailRequest.builder()
			.source(fromEmail) // 발신자
			.destination(Destination.builder().toAddresses(toEmail).build()) // 수신자
			.message(Message.builder() // 메일 본문
				.subject(Content.builder().charset("UTF-8").data(subject).build())
				.body(Body.builder()
					.html(Content.builder().charset("UTF-8").data(htmlBody).build())
					.build())
				.build())
			.build();

		sesClient.sendEmail(request); // ASW SES에 전송 요청
	}
}
