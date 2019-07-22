// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.access;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorDeletesUser;

@Service
public class ScanRevokeUserAccessAtAllService {

	private static final Logger LOG = LoggerFactory.getLogger(ScanRevokeUserAccessAtAllService.class);

	
	@Autowired
	ScanAccessRepository repository;
	
	@Transactional
	@UseCaseAdministratorDeletesUser(@Step(number=3,name="revoke user from schedule access"))
	public void revokeUserAccess(String userId) {
		repository.deleteAcessForUserAtAll(userId);
		
		LOG.info("Revoked access at all for user:{}",userId);
	}

	
}
