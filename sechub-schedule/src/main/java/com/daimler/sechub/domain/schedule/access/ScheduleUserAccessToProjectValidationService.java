// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.access;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.logging.SecurityLogService;
import com.daimler.sechub.sharedkernel.logging.SecurityLogType;

@Service
public class ScheduleUserAccessToProjectValidationService {

	@Autowired
	ScheduleAccessRepository accessRepository;

	@Autowired
	UserContextService userContextService;

	@Autowired
	SecurityLogService securityLogService;

	@Autowired
	LogSanitizer logSanitizer;

	/**
	 * Assert user logged in has access to project
	 * @param projectId
	 */
	public void assertUserHasAccessToProject(String projectId) {
				
		if (userContextService.isSuperAdmin()) {
			/* a super admin has always access to existing projects */
			
			if(!accessRepository.hasProjectUserAccess(projectId)) {
				throw new NotFoundException("Project " + projectId + " does not exist, or no user has access at all.");
			}
			
			return;
		}
		String userId = userContextService.getUserId();

		ProjectAccessCompositeKey key = new ProjectAccessCompositeKey(userId, projectId);
		Optional<ScheduleAccess> scheduleAccess = accessRepository.findById(key);
		if (!scheduleAccess.isPresent()) {
			securityLogService.log(SecurityLogType.POTENTIAL_INTRUSION, "Denied user access in domain 'schedule'. userId={},projectId={}",userId,logSanitizer.sanitize(projectId,30));
			// we say "... or you have no access - just to obfuscate... so it's not clear to
			// bad guys they got a target...
			throw new NotFoundException("Project " + projectId + " does not exist, or you have no access.");
		}
	}

}
