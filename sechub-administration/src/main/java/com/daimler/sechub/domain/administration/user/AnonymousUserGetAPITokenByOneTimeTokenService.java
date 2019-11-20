// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.administration.APITokenGenerator;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.logforgery.LogSanitizer;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.user.UseCaseUserClicksLinkToGetNewAPIToken;

@Service
public class AnonymousUserGetAPITokenByOneTimeTokenService {

	private static final Logger LOG = LoggerFactory.getLogger(AnonymousUserGetAPITokenByOneTimeTokenService.class);

	private static final long DEFAULT_OUTDATED_TIME_MILLIS = 86400000;// 1d * 24h * 60m * 60s * 1000ms = one day = 86400000

	@Value("${sechub.user.onetimetoken.outdated.millis:86400000}")
	@MustBeDocumented(value="One time token time when outdating")
	long oneTimeOutDatedMillis = DEFAULT_OUTDATED_TIME_MILLIS;

	@Autowired
	DomainMessageService eventBusService;

	@Autowired
	UserRepository sechubUserRepository;

	@Autowired
	APITokenGenerator apiTokenGenerator;

	@Autowired
	LogSanitizer logSanitizer;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@UseCaseUserClicksLinkToGetNewAPIToken(@Step(number=2,next={3,4}, name="Validation and update",description="When its a valid one time token a new api token is generated and persisted hashed to user. The token itself is returned. When not valid an emtpy string is the result ..."))
	@IsSendingAsyncMessage(MessageID.USER_API_TOKEN_CHANGED)
	public String createNewAPITokenForUserByOneTimeToken(String oneTimeToken) {
		if (oneTimeToken==null) {
			return "";
		}
		if (oneTimeToken.isEmpty()) {
			return "";
		}
		Optional<User> found = sechubUserRepository.findByOneTimeToken(oneTimeToken);
		if (! found.isPresent()) {
			LOG.warn(
					"Did not found a user having one time token :{}. Maybe an attack, so will just return empty string...",
					logSanitizer.sanitize(oneTimeToken,50));
			return "";
		}

		User user = found.get();
		/* check not outdated onetime token*/
		if (user.isOneTimeTokenOutDated(oneTimeOutDatedMillis)) {
			LOG.warn(
					"Did found a user having one time token :{}, but token is outdated! Maybe an attack, so will just return empty string... and keep the old entry as is",
					logSanitizer.sanitize(oneTimeToken,50));
			return "";
		}

		user.oneTimeToken=null;
		user.oneTimeTokenDate=null;
		String rawToken = apiTokenGenerator.generateNewAPIToken();
		user.hashedApiToken=passwordEncoder.encode(rawToken);

		sechubUserRepository.save(user);


		DomainMessage request = new DomainMessage(MessageID.USER_API_TOKEN_CHANGED);
		UserMessage message = new UserMessage();
		message.setEmailAdress(user.getEmailAdress());
		message.setUserId(user.getName());
		message.setHashedApiToken(user.getHashedApiToken());

		request.set(MessageDataKeys.USER_API_TOKEN_DATA, message);
		eventBusService.sendAsynchron(request);

		/* we return the raw token to user - but do NOT save it but hashed variant */
		return rawToken;
	}


}
