// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.access;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorDeletesUser;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScheduleRevokeUserAccessAtAllService {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduleRevokeUserAccessAtAllService.class);


	@Autowired
	ScheduleAccessRepository repository;

	@Autowired
	UserInputAssertion assertion;

	@Transactional
	@UseCaseAdministratorDeletesUser(@Step(number=3,name="revoke user from schedule access"))
	public void revokeUserAccess(String userId) {
		assertion.isValidUserId(userId);

		repository.deleteAccessForUserAtAll(userId);

		LOG.info("Revoked access at all for user:{}",userId);
	}


}
