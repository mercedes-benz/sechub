// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorListsAllAdmins;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorListsAllUsers;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserListService {

	@Autowired
	UserRepository userRepository;

	/* @formatter:off */
	@UseCaseAdministratorListsAllUsers(@Step(number=2,name="Service call",description="All userids of sechub users are returned as json"))
	public List<String> listUsers() {
		/* @formatter:on */
		return userRepository.findAll().stream().map(User::getName).collect(Collectors.toList());
	}

	/* @formatter:off */
	@UseCaseAdministratorListsAllAdmins(@Step(number=2,name="Service call",description="All userids of sechub administrators are returned as json"))
	public List<String> listAdministrators() {
		/* @formatter:on */
		User userExample = new User();
		userExample.superAdmin=true;
		return userRepository.findAll(Example.of(userExample)).stream().map(User::getName).collect(Collectors.toList());
	}


}
