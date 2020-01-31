package com.daimler.sechub.domain.notification.email;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;

public class SimpleMailMessageSupportTest {

	private SimpleMailMessageSupport supportToTest;
	private SimpleMailMessage message;

	@Before
	public void before() {
		supportToTest = new SimpleMailMessageSupport();
		message = mock(SimpleMailMessage.class);
	}

	@Test
	public void when_nothing_is_set_subject_null_is_only_part_in_result() {
		/* execute */
		String result = supportToTest.describeTopic(message);

		/* test */
		assertEquals("subject=null;", result);
	}
	
	@Test
	public void when_text_is_set_subject_null_is_only_part_in_result() {
		/* prepare */
		when(message.getText()).thenReturn("potential sensitive data");

		
		/* execute */
		String result = supportToTest.describeTopic(message);

		/* test */
		assertEquals("subject=null;", result);
	}

	@Test
	public void when_subject_is_set_and_one_target_to_only_those_parts_are_in_result() {
		/* prepare */
		when(message.getSubject()).thenReturn("subject from test");
		when(message.getTo()).thenReturn(new String[] { "test1@example.com" });

		/* execute */
		String result = supportToTest.describeTopic(message);

		/* test */
		assertEquals("subject=subject from test; to=test1@example.com;", result);
	}
	
	
	@Test
	public void when_subject_is_set_and_one_target_to_and_from_and_reply_only_those_parts_are_in_result() {
		/* prepare */
		when(message.getSubject()).thenReturn("subject from test");
		when(message.getTo()).thenReturn(new String[] { "test1@example.com" });
		when(message.getFrom()).thenReturn("sender1@example.com" );
		when(message.getReplyTo()).thenReturn("reply1@example.com" );
		
		/* execute */
		String result = supportToTest.describeTopic(message);
		
		/* test */
		assertEquals("subject=subject from test; to=test1@example.com; from=sender1@example.com; replyTo=reply1@example.com;", result);
	}
	@Test
	public void when_subject_is_set_and_one_target_to_and_from_only_those_parts_are_in_result() {
		/* prepare */
		when(message.getSubject()).thenReturn("subject from test");
		when(message.getTo()).thenReturn(new String[] { "test1@example.com" });
		when(message.getFrom()).thenReturn("sender1@example.com" );

		/* execute */
		String result = supportToTest.describeTopic(message);

		/* test */
		assertEquals("subject=subject from test; to=test1@example.com; from=sender1@example.com;", result);
	}

	@Test
	public void when_subject_is_set_and_two_target_to_only_those_parts_are_in_result() {
		/* prepare */
		when(message.getSubject()).thenReturn("subject from test");
		when(message.getTo()).thenReturn(new String[] { "test1@example.com", "test2@example.com" });

		/* execute */
		String result = supportToTest.describeTopic(message);

		/* test */
		assertEquals("subject=subject from test; to=test1@example.com,test2@example.com;", result);
	}

	@Test
	public void when_subject_is_set_and_2_to_1_cc_1_bcc_targets_all_of_these_parts_are_in_result() {
		/* prepare */
		when(message.getSubject()).thenReturn("subject from test");
		when(message.getTo()).thenReturn(new String[] { "test1@example.com", "test2@example.com" });
		when(message.getCc()).thenReturn(new String[] { "test3@example.com" });
		when(message.getBcc()).thenReturn(new String[] { "test4@example.com" });

		/* execute */
		String result = supportToTest.describeTopic(message);

		/* test */
		assertEquals("subject=subject from test; to=test1@example.com,test2@example.com; cc=test3@example.com; bcc=test4@example.com;", result);
	}
	
	@Test
	public void when_text_is_set_subject_is_set_and_2_to_1_cc_1_bcc_targets_all_of_these_parts_are_in_result_except_text() {
		/* prepare */
		when(message.getSubject()).thenReturn("subject from test");
		when(message.getText()).thenReturn("sensitive data");
		when(message.getTo()).thenReturn(new String[] { "test1@example.com", "test2@example.com" });
		when(message.getCc()).thenReturn(new String[] { "test3@example.com" });
		when(message.getBcc()).thenReturn(new String[] { "test4@example.com" });

		/* execute */
		String result = supportToTest.describeTopic(message);

		/* test */
		assertEquals("subject=subject from test; to=test1@example.com,test2@example.com; cc=test3@example.com; bcc=test4@example.com;", result);
	}

	@Test
	public void when_subject_is_set_and_2_to_2_cc_2_bcc_targets_all_of_these_parts_are_in_result() {
		/* prepare */
		when(message.getSubject()).thenReturn("subject from test");
		when(message.getTo()).thenReturn(new String[] { "test1@example.com", "test2@example.com" });
		when(message.getCc()).thenReturn(new String[] { "test3@example.com", "test5@example.com" });
		when(message.getBcc()).thenReturn(new String[] { "test4@example.com", "test6@example.com" });

		/* execute */
		String result = supportToTest.describeTopic(message);

		/* test */
		assertEquals(
				"subject=subject from test; to=test1@example.com,test2@example.com; cc=test3@example.com,test5@example.com; bcc=test4@example.com,test6@example.com;",
				result);
	}

}
