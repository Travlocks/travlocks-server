package org.umc.travlocksserver.global.mail;

public interface MailSender {
	void send(String toEmail, String subject, String htmlBody);
}
