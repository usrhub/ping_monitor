package de.steinerix.ping_monitor.config;

import java.net.InetAddress;

import javax.mail.internet.InternetAddress;

/**
 * A SMTP Mail configuration. ALL builder methods have to be invoked in order to
 * build an object.
 * 
 * @author usr
 *
 */
public class MailConfig {

	/**
	 * Authentication method
	 * 
	 * @author usr
	 *
	 */
	public enum AuthType {
		NONE, PASSWORD // TODO: implement ENCRYPTED in a future release
	};

	/**
	 * Security type
	 * 
	 * @author usr
	 *
	 */
	public enum SecurityType {
		NONE, SSL_TLS, STARTTLS
	};

	private final boolean enabled;
	private final InetAddress smtpServer;
	private final int port;
	private final String username;
	private final String password;
	private final AuthType authType;
	private final SecurityType securityType;
	private final InternetAddress from;

	public static class Builder {
		// Required
		private boolean enabled;
		private InetAddress smtpServer;
		private int port;
		private AuthType authType;
		private SecurityType securityType;
		private String username;
		private String password;
		private InternetAddress from;

		/** Required - set server, port and define if mailing is enabled */
		public Builder server(InetAddress smtpServer, int port, boolean enabled) {
			this.smtpServer = smtpServer;
			this.port = port;
			this.enabled = enabled;
			return this;
		}

		/** Required - set AuthType and SecurityType */
		public Builder type(AuthType authType, SecurityType securityType) {
			this.authType = authType;
			this.securityType = securityType;
			return this;
		}

		/**
		 * Required - set from address, username and password (username and
		 * password may be an empty string if authType is NONE)
		 */
		public Builder credentials(InternetAddress from, String username,
				String password) {
			this.username = username;
			this.password = password;
			this.from = from;
			return this;
		}

		/** build MailConfig */
		public MailConfig build() {
			return new MailConfig(this);
		}

	}

	private MailConfig(Builder builder) {
		// check arguments
		if (builder.smtpServer == null || builder.authType == null
				|| builder.securityType == null || builder.from == null
				|| builder.username == null || builder.password == null) {
			throw new IllegalStateException(
					"Not all required parameters of MailConfig set. Please call ALL builder methods. (arguments must be != null)");
		} else if (!(builder.authType == AuthType.NONE)
				&& ("username".equals("") || "password".equals(""))) {
			throw new IllegalStateException(
					"Password and username required with this AuthType.");
		}

		enabled = builder.enabled;
		smtpServer = builder.smtpServer;
		port = builder.port;
		username = builder.username;
		password = builder.password;
		authType = builder.authType;
		securityType = builder.securityType;
		from = builder.from;
	}

	/** Is mail module enabled */
	public boolean isEnabled() {
		return enabled;
	}

	/** SMTP host */
	public InetAddress getSmtpServer() {
		return smtpServer;
	}

	/** Port of SMTP host */
	public int getPort() {
		return port;
	}

	/** username */
	public String getUsername() {
		return username;
	}

	/** password */
	public String getPassword() {
		return password;
	}

	/** Email Address (sender) */
	public InternetAddress getFrom() {
		return from;
	}

	/** Authorisation method */
	public AuthType getAuthType() {
		return authType;
	}

	/** Security method */
	public SecurityType getSecurityType() {
		return securityType;
	}
}
