package com.daimler.sechub.domain.notification.email;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class SMTPServerConfigurationTest {

	private SMTPServerConfiguration configToTest;
	private JavaMailSenderImpl mockedMailSender;
	private Properties properties;

	@Before
	public void before() {
		properties = new Properties();
		mockedMailSender = mock(JavaMailSenderImpl.class);
		when(mockedMailSender.getJavaMailProperties()).thenReturn(properties);
		configToTest = new SMTPServerConfiguration() {
			@Override
			protected JavaMailSenderImpl createMailSender() {
				return mockedMailSender;
			}
		};
	}


	@Test
	public void defaults() {
		/* prepare */
		configToTest.hostname="host1";

		/* execute */
		configToTest.getJavaMailSender();

		/* test */
		verify(mockedMailSender,never()).setPassword(any());
		verify(mockedMailSender,never()).setUsername(any());
		verify(mockedMailSender).setHost("host1");
		verify(mockedMailSender).setPort(SMTPServerConfiguration.DEFAULT_SMTP_SERVER_PORT);

		assertEquals(2, properties.size());
		assertEquals("smtp", properties.getProperty("mail.transport.protocol"));
		assertEquals("false", properties.getProperty("mail.smtp.auth"));

	}

	@Test
	public void all_configured_all_set_to_mailsender() {
		/* prepare */
		configToTest.hostname="host1";
		configToTest.hostPort=1234;
		configToTest.username="usr1";
		configToTest.password="pwd1";
		configToTest.smtpConfigString="mail.smtp.auth=true,mail.smtp.timeout=4321,mail.transport.protocol=smtp";

		/* execute */
		configToTest.getJavaMailSender();

		/* test */
		verify(mockedMailSender).setPassword("pwd1");
		verify(mockedMailSender).setUsername("usr1");
		verify(mockedMailSender).setPort(1234);
		verify(mockedMailSender).setHost("host1");

		assertEquals(3, properties.size());
		assertEquals("smtp", properties.getProperty("mail.transport.protocol"));
		assertEquals("true", properties.getProperty("mail.smtp.auth"));
		assertEquals("4321", properties.getProperty("mail.smtp.timeout"));

	}

	@Test
	public void all_configured_except_credentails_all_set_to_mailsender_but_credentials_not_set() {
		/* prepare */
		configToTest.hostname="host1";
		configToTest.hostPort=1234;
		configToTest.username="";
		configToTest.password="";
		configToTest.smtpConfigString="mail.smtp.auth=false,mail.smtp.timeout=4321";

		/* execute */
		configToTest.getJavaMailSender();

		/* test */
		verify(mockedMailSender,never()).setPassword(any());
		verify(mockedMailSender,never()).setUsername(any());
		verify(mockedMailSender).setPort(1234);
		verify(mockedMailSender).setHost("host1");

		assertEquals(2, properties.size());
		assertEquals("false", properties.getProperty("mail.smtp.auth"));
		assertEquals("4321", properties.getProperty("mail.smtp.timeout"));

	}

}
