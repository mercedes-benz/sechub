// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminDeletesSignup;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminListsOpenUserSignups;

import jakarta.annotation.security.RolesAllowed;

/**
 * Self registration rest controller - restricted access to super admins
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
public class SignupAdministrationRestController {
    @Autowired
    private SignupRepository repository;

    @Autowired
    private SignupDeleteService deleteService;

    /* @formatter:off */
	@UseCaseAdminDeletesSignup(@Step(number=1, name="Rest API call",description="Rest api called to remove user signup",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_DELETE_SIGNUP, method = RequestMethod.DELETE, produces= {MediaType.APPLICATION_JSON_VALUE})
	public void deleteSignup(@PathVariable(name="userId") String userId) {
		deleteService.delete(userId);
		/* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdminListsOpenUserSignups(@Step(number=1,name="Rest call",description="All self registrations are returned as json",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_LIST_USER_SIGNUPS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public List<Signup> listUserSignups() {
		/* @formatter:on */
        return repository.findAll();
    }

}
