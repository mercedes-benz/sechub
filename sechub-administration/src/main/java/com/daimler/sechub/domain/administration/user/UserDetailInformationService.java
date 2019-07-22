// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorShowsUserDetails;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserDetailInformationService {

	private static final Logger LOG = LoggerFactory.getLogger(UserDetailInformationService.class);

	@Autowired
	UserRepository userRepository;
	
	/* @formatter:off */
	@Validated
	@UseCaseAdministratorShowsUserDetails(
			@Step(
				number = 2, 
				name = "Service fetches user details.", 
				description = "The service will fetch user details"))
	/* @formatter:on */
	public UserDetailInformation fetchDetails(String userId) {
		LOG.debug("fetching user details for user:{}",userId);
		User user = userRepository.findOrFailUser(userId);
		
		return new UserDetailInformation(user);		
	}
}
