// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;

@Service
@Profile(Profiles.MOCKED_NOTIFICATIONS)
public class MockEmailService implements EmailService {

	private static final Logger LOG = LoggerFactory.getLogger(MockEmailService.class);

	@Autowired
	LogSanitizer logSanitizer;

	@MustBeDocumented("When email mock shall cache the mails this must be configured to true, per default disabled!")
	@Value("${sechub.notification.email.mock.cache.enabled:false}")
	private boolean cacheEmailsEnabled;

	private Map<String, List<SimpleMailMessage>> mails = new HashMap<>();

	public boolean isCacheEmailsEnabled() {
		return cacheEmailsEnabled;
	}

	@Override
	public void send(SimpleMailMessage message) {
		if (!cacheEmailsEnabled) {
			return;
		}
		addMessages(message, MailTargetType.TO);
		addMessages(message, MailTargetType.CC);
		addMessages(message, MailTargetType.BCC);

	}

	private enum MailTargetType {
		TO, CC, BCC
	}

	private void addMessages(SimpleMailMessage message, MailTargetType targetType) {
		String[] targetMailAddresses;
		switch (targetType) {
		case TO:
			targetMailAddresses = message.getTo();
			break;
		case CC:
			targetMailAddresses = message.getCc();
			break;
		case BCC:
			targetMailAddresses = message.getBcc();
			break;
		default:
			targetMailAddresses = null;
		}
		if (targetMailAddresses == null) {
			return;
		}
		for (String target : targetMailAddresses) {
			List<SimpleMailMessage> mailsInternal = getMailsInternal(target);
			mailsInternal.add(message);
			LOG.info("Send {} target:{} [ {} ]: subject:'{}', ", targetType, target, mailsInternal.size(), message.getSubject());
		}
	}

	public List<SimpleMailMessage> getMailsFor(String emailAdress) {
		if (!cacheEmailsEnabled) {
			LOG.debug("cache for emails is disabled, so returning empty mails list for emailAdress:{}", logSanitizer.sanitize(emailAdress, -1));
			return Collections.emptyList();
		}
		List<SimpleMailMessage> copy = new ArrayList<>();
		copy.addAll(getMailsInternal(emailAdress));
		return copy;
	}

	private List<SimpleMailMessage> getMailsInternal(String emailAdress) {
		synchronized (mails) {
			List<SimpleMailMessage> list = mails.computeIfAbsent(emailAdress, this::createMailList);
			LOG.info("resolved messages:{} for user:{}", list.size(), logSanitizer.sanitize(emailAdress, -1));
			return list;
		}

	}

	private List<SimpleMailMessage> createMailList(/* NOSONAR */String emailAdress) {
		return new ArrayList<>();
	}

	public void resetMockMails() {
		synchronized (mails) {
			mails.clear();
		}
	}

}
