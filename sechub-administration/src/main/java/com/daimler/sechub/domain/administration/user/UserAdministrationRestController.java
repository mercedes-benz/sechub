// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.domain.administration.AdministrationAPIConstants;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.signup.UseCaseAdministratorAcceptsSignup;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorDeletesUser;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorGrantsAdminRightsToUser;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorListsAllAdmins;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorListsAllUsers;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorRevokesAdminRightsFromAdmin;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorShowsUserDetails;

/**
 * The rest api for user administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserAdministrationRestController {

	@Autowired
	UserCreationService creationService;

	@Autowired
	UserDeleteService deleteService;

	@Autowired
	UserDetailInformationService detailsService;

	@Autowired
	UserListService userListService;

	@Autowired
	UserGrantSuperAdminRightsService userGrantSuperAdminRightsService;

	@Autowired
	UserRevokeSuperAdminRightsService userRevokeSuperAdminRightsService;

	/* @formatter:off */
	@UseCaseAdministratorAcceptsSignup(@Step(number=1,name="Rest call", description="Administrator accepts a persisted self registration entry by calling rest api",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_ACCEPT_USER_SIGNUP, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.CREATED)
	public void acceptUserSignUp(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
		creationService.createUserFromSelfRegistration(userId);
	}

	/* @formatter:off */
	@UseCaseAdministratorListsAllUsers(@Step(number=1,name="Rest call",description="All userids of sechub users are returned as json",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_LIST_ALL_USERS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public List<String> listUsers() {
		/* @formatter:on */
		return userListService.listUsers();
	}

	/* @formatter:off */
	@UseCaseAdministratorListsAllAdmins(@Step(number=1,name="Rest call",description="All userids of sechub administrators are returned as json",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_LIST_ALL_ADMINS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public List<String> listAdministrators() {
		/* @formatter:on */
		return userListService.listAdministrators();
	}

	/* @formatter:off */
	@UseCaseAdministratorShowsUserDetails(@Step(number=1,name="Rest call",description="Json returned containing details about user and her/his projects",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SHOW_USER_DETAILS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public UserDetailInformation showUserDetails(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
		return detailsService.fetchDetails(userId);
	}

	/* @formatter:off */
	@UseCaseAdministratorDeletesUser(@Step(number=1,name="Rest call",description="User will be deleted",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_DELETE_USER, method = RequestMethod.DELETE, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public void deleteUser(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
		deleteService.deleteUser(userId);
	}

	/* @formatter:off */
	@UseCaseAdministratorGrantsAdminRightsToUser(@Step(number=1,name="Rest call",description="User will be granted admin rights",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_GRANT_ADMIN_RIGHTS_TO_USER, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public void grantSuperAdminrights(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
		userGrantSuperAdminRightsService.grantSuperAdminRightsFor(userId);
	}

	/* @formatter:off */
	@UseCaseAdministratorRevokesAdminRightsFromAdmin(@Step(number=1,name="Rest call",description="Admin rights will be revoked from admin",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_REVOKE_ADMIN_RIGHTS_FROM_USER, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public void revokeSuperAdminrights(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
		userRevokeSuperAdminRightsService.revokeSuperAdminRightsFrom(userId);
	}

}
