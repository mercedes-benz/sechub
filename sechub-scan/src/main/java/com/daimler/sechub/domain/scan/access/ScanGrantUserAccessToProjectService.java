// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.access;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdminAssignsUserToProject;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScanGrantUserAccessToProjectService {



	private static final Logger LOG = LoggerFactory.getLogger(ScanGrantUserAccessToProjectService.class);


	@Autowired
	ScanAccessRepository repository;

	@Autowired
	UserInputAssertion assertion;

	@UseCaseAdminAssignsUserToProject(@Step(number=3,name="Update scan authorization parts"))
	public void grantUserAccessToProject(String userId, String projectId) {
		assertion.isValidUserId(userId);
		assertion.isValidProjectId(projectId);

		ScanAccess scanAccess = new ScanAccess(userId,projectId);
		Optional<ScanAccess> potentialAlreadyFound = repository.findById(scanAccess.getKey());
		if (potentialAlreadyFound.isPresent()) {
			LOG.debug("User {} has already acces to {} so skipped",userId,projectId);
			return;
		}
		repository.save(scanAccess);
		LOG.debug("User {} has now gained acces to {}d",userId,projectId);
	}


}
