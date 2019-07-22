// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.Profiles;

@Service
@Profile(Profiles.MOCKED_NOTIFICATIONS)
public class MockEmailService implements EmailService{


	private static final Logger LOG = LoggerFactory.getLogger(MockEmailService.class);

	@MustBeDocumented("When email mock shall cache the mails this must be configured to true, per default disabled!")
	@Value("${sechub.notification.email.mock.cache.enabled:false}")
	private boolean cacheEmailsEnabled;

	private Map<String,List<SimpleMailMessage>> mails = new HashMap<>();

	public boolean isCacheEmailsEnabled() {
		return cacheEmailsEnabled;
	}

	@Override
	public void send(SimpleMailMessage message) {
		if (!cacheEmailsEnabled) {
			return;
		}
		String[] toUsers = message.getTo();
		for (String to: toUsers) {
			List<SimpleMailMessage> mailsInternal = getMailsInternal(to);
			mailsInternal.add(message);
			LOG.info("add message:{} for user:{}. Has now {} mails.",message.getSubject(),to,mailsInternal.size());
		}
	}

	public List<SimpleMailMessage> getMailsFor(String emailAdress) {
		if (!cacheEmailsEnabled) {
			LOG.debug("cache for emails is disabled, so returning empty mails list for emailAdress:{}",emailAdress);
			return Collections.emptyList();
		}
		return getMailsInternal(emailAdress);
	}

	private List<SimpleMailMessage> getMailsInternal(String emailAdress) {
		List<SimpleMailMessage> list = mails.computeIfAbsent(emailAdress,this::createMailList);
		LOG.info("resolved messages:{} for user:{}",list.size(),emailAdress);
		return list;
	}


	private List<SimpleMailMessage> createMailList(/*NOSONAR*/String emailAdress) {
		return new ArrayList<>();
	}

	public void resetMockMails() {
		mails.clear();
	}

}
