package com.daimler.sechub.domain.notification.email;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;

import com.daimler.sechub.domain.notification.NotificationConfiguration;


public class MailMessageFactoryTest {

	private MailMessageFactory factoryToTest;
	private NotificationConfiguration mockedConfiguration;

	@Before
	public void before() {
		mockedConfiguration=mock(NotificationConfiguration.class);
		factoryToTest = new MailMessageFactory();
		factoryToTest.configuration=mockedConfiguration;
	}

	@Test
	public void subject_contained_and_date() {
		/* execute */
		SimpleMailMessage message = factoryToTest.createMessage("subject1");

		/* test */
		assertEquals("subject1",message.getSubject());
		assertNotNull(message.getSentDate());
	}

	@Test
	public void noreply_set_is_also_in_message() {
		/* prepare */
		when(mockedConfiguration.getEmailReplyTo()).thenReturn("reply1");

		/* execute */
		SimpleMailMessage message = factoryToTest.createMessage("subject1");

		/* test */
		assertEquals("reply1", message.getReplyTo());
	}

	@Test
	public void no_noreply_set_is_also_not_in_message() {
		/* prepare */
		when(mockedConfiguration.getEmailReplyTo()).thenReturn(null);

		/* execute */
		SimpleMailMessage message = factoryToTest.createMessage("subject1");

		/* test */
		assertEquals(null, message.getReplyTo());
	}

}
