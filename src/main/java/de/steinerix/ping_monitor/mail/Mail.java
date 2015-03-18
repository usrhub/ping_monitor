package de.steinerix.ping_monitor.mail;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.steinerix.ping_monitor.config.MailConfig;
import de.steinerix.ping_monitor.config.MailConfig.AuthType;
import de.steinerix.ping_monitor.config.MailConfig.SecurityType;

public class Mail {
	private static Logger log = Logger.getLogger(MailConfig.class.getName());

	//
	// InetAddress smtpServer, int port,
	// String username, String password, AuthType authType,
	// SecurityType securityType, InternetAddress from, InternetAddress to

	/**
	 * send message with provided MailConfig, To address, subject and body
	 * 
	 * @throws MessagingException
	 */
	public static void sendMessage(MailConfig config, InternetAddress to,
			String subject, String body) throws MessagingException {

		log.log(Level.INFO, "Send new message with " + config.getSmtpServer()
				+ " to " + to.getAddress());

		Properties properties = new Properties();

		properties.put("mail.smtp.host", config.getSmtpServer().getHostName()); // SMTP
																				// Host
		properties.put("mail.smtp.port", config.getPort()); // TLS Port

		if (config.getSecurityType() == SecurityType.SSL_TLS) { // SSL/TLS
			properties.put("mail.smtp.socketFactory.port", "port");
			properties.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
		} else if (config.getSecurityType() == SecurityType.STARTTLS) {
			properties.put("mail.smtp.starttls.enable", "true"); // StartTLS
		}

		Session session;

		if (config.getAuthType() == AuthType.PASSWORD) {
			properties.put("mail.smtp.auth", "true"); // enable authentication

			Authenticator authenticator = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(config.getUsername(),
							config.getPassword());
				}
			};

			session = Session.getInstance(properties, authenticator);
		} else {
			session = Session.getInstance(properties);
		}

		send(session, config.getFrom(), to, subject, body);
	}

	/**
	 * send message
	 * 
	 * @throws MessagingException
	 */
	private static void send(Session session, InternetAddress from,
			InternetAddress to, String subject, String body)
			throws MessagingException {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(from);
		message.addRecipient(Message.RecipientType.TO, to);
		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);

	}
}
