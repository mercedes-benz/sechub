// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminListsAllAdmins;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminListsAllUsers;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserListService {

    @Autowired
    UserRepository userRepository;

    /* @formatter:off */
	@UseCaseAdminListsAllUsers(@Step(number=2,name="Service call",description="All userids of sechub users are returned as json"))
	public List<String> listUsers() {
		/* @formatter:on */
        return userRepository.findAll().stream().map(User::getName).collect(Collectors.toList());
    }

    /* @formatter:off */
	@UseCaseAdminListsAllAdmins(@Step(number=2,name="Service call",description="All userids of sechub administrators are returned as json"))
	public List<String> listAdministrators() {
		/* @formatter:on */
        User userExample = new User();
        userExample.superAdmin = true;
        return userRepository.findAll(Example.of(userExample)).stream().map(User::getName).collect(Collectors.toList());
    }

}
