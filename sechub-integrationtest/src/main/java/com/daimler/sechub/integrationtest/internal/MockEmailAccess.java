// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.daimler.sechub.integrationtest.api.MockEmailEntry;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.test.TestURLBuilder;

public class MockEmailAccess {

	static MockEmailAccess mailAccess() {
		return new MockEmailAccess();
	}

	public List<Map<String, Object>> getMailsFor(TestUser user) {
		return getMailsFor(user.getEmail());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String, Object>> getMailsFor(String mailAdress) {
		String url = getUrlBuilder().buildFetchEmailsFromMockMailServiceUrl(mailAdress);
		ResponseEntity<List> result = getTemplate().getForEntity(url, List.class);
		return result.getBody();
	}

	public List<MockEmailEntry> getMockMailListFor(String mailAdress) {
		return convertToMockMailList(getMailsFor(mailAdress));
	}

	private RestTemplate getTemplate() {
		return getContext().getTemplateForSuperAdmin().getTemplate();
	}

	private TestURLBuilder getUrlBuilder() {
		return getContext().getUrlBuilder();
	}

	private IntegrationTestContext getContext() {
		return IntegrationTestContext.get();
	}

	public MockEmailEntry findMailOrFail(TestUser toUser, String subject) {
		return findMailOrFail(toUser, subject, 3);
	}

	public MockEmailEntry findMailOrFail(String email, String subject) {
		return findMailOrFail(email, subject, 3);
	}

	public MockEmailEntry findMailOrFail(TestUser toUser, String subject, int maxSecondsToWait) {
		return findMailOrFail(toUser.getEmail(), subject, maxSecondsToWait);
	}

	public MockEmailEntry findMailOrFail(String email, String subjectRegexp, int maxSecondsToWait) {
		MockEmailEntry found = null;
		List<MockEmailEntry> list = null;
		for (int i = 0; i < maxSecondsToWait; i++) {
			List<Map<String, Object>> listAsMap = getMailsFor(email);
			list = convertToMockMailList(listAsMap);

			for (MockEmailEntry message : list) {
				if (subjectRegexp == null || subjectRegexp.isEmpty() || message.subject.matches(subjectRegexp)) {
					found = message;
					break;
				}
			}
			if (found != null) {
				break;
			}
			try {
				/* NOSONAR */Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (found == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Did not found mail containing:\n-emailadress (TO/CC/BCC): ");
			sb.append(email);
			sb.append("\n-subject: '");
			sb.append(subjectRegexp);
			sb.append("'\n\nFound mails for this email adress:");
			for (MockEmailEntry message : list) {
				sb.append("\n").append(message.toString());
			}
			fail(sb.toString());
		}
		return found;
	}

	public static final List<MockEmailEntry> convertToMockMailList(List<Map<String, Object>> listAsMap) {
		List<MockEmailEntry> list;
		list = new ArrayList<>();
		for (Map<String, Object> map : listAsMap) {
			MockEmailEntry entry = new MockEmailEntry();
			entry.from = map.get("from").toString();
			entry.subject = map.get("subject").toString();
			entry.text = map.get("text").toString();
			Object to = map.get("to");
			if (to != null) {
				entry.to = to.toString();
			}
			Object cc = map.get("cc");
			if (cc != null) {
				entry.cc = cc.toString();
			}
			Object bcc = map.get("bcc");
			if (bcc != null) {
				entry.bcc = bcc.toString();
			}
			list.add(entry);
		}
		return list;
	}

	public void reset() {
		getTemplate().delete(getUrlBuilder().buildResetAllMockMailsUrl());
	}

}
