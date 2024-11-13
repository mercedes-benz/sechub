// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminAcceptsSignup;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminDeletesUser;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminGrantsAdminRightsToUser;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminListsAllAdmins;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminListsAllUsers;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminRevokesAdminRightsFromAdmin;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminShowsUserDetails;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminShowsUserDetailsForEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUpdatesUserEmailAddress;

import jakarta.annotation.security.RolesAllowed;

/**
 * The rest api for user administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
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

    @Autowired
    UserEmailAddressUpdateService userEmailAddressUpdateService;

    /* @formatter:off */
	@UseCaseAdminAcceptsSignup(@Step(number=1,name="Rest call", description="Administrator accepts a persisted self registration entry by calling rest api",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_ACCEPT_USER_SIGNUP, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.CREATED)
	public void acceptUserSignUp(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
        creationService.createUserFromSelfRegistration(userId);
    }

    /* @formatter:off */
	@UseCaseAdminListsAllUsers(@Step(number=1,name="Rest call",description="All userids of sechub users are returned as json",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_LIST_ALL_USERS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public List<String> listUsers() {
		/* @formatter:on */
        return userListService.listUsers();
    }

    /* @formatter:off */
	@UseCaseAdminListsAllAdmins(@Step(number=1,name="Rest call",description="All userids of sechub administrators are returned as json",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_LIST_ALL_ADMINS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public List<String> listAdministrators() {
		/* @formatter:on */
        return userListService.listAdministrators();
    }

    /* @formatter:off */
	@UseCaseAdminShowsUserDetails(@Step(number=1,name="Rest call",description="Json returned containing details about user and her/his projects",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SHOW_USER_DETAILS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public UserDetailInformation showUserDetails(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
        return detailsService.fetchDetails(userId);
    }

    /* @formatter:off */
	@UseCaseAdminShowsUserDetailsForEmailAddress(@Step(number=1,name="Rest call",description="Json returned containing details about user and her/his projects",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SHOW_USER_BY_EMAIL_DETAILS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public UserDetailInformation showUserDetailsForEmailAddress(@PathVariable(name="emailAddress") String emailAddress) {
	    /* @formatter:on */
        return detailsService.fetchDetailsByEmailAddress(emailAddress);
    }

    /* @formatter:off */
	@UseCaseAdminDeletesUser(@Step(number=1,name="Rest call",description="User will be deleted",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_DELETE_USER, method = RequestMethod.DELETE, produces= {MediaType.APPLICATION_JSON_VALUE})
	public void deleteUser(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
        deleteService.deleteUser(userId);
    }

    /* @formatter:off */
	@UseCaseAdminGrantsAdminRightsToUser(@Step(number=1,name="Rest call",description="User will be granted admin rights",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_GRANT_ADMIN_RIGHTS_TO_USER, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	public void grantSuperAdminRights(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
        userGrantSuperAdminRightsService.grantSuperAdminRightsFor(userId);
    }

    /* @formatter:off */
	@UseCaseAdminRevokesAdminRightsFromAdmin(@Step(number=1,name="Rest call",description="Admin rights will be revoked from admin",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_REVOKE_ADMIN_RIGHTS_FROM_USER, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	public void revokeSuperAdminRights(@PathVariable(name="userId") String userId) {
		/* @formatter:on */
        userRevokeSuperAdminRightsService.revokeSuperAdminRightsFrom(userId);
    }

    /* @formatter:off */
    @UseCaseAdminUpdatesUserEmailAddress(@Step(number=1,name="Rest call",description="User emaill address will be changed",needsRestDoc=true))
    @RequestMapping(path = AdministrationAPIConstants.API_UPDATE_USER_EMAIL_ADDRESS, method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
    public void updateUserEmailAddress(@PathVariable(name="userId") String userId,@PathVariable(name="newEmailAddress") String newEmailAddress) {
        /* @formatter:on */
        userEmailAddressUpdateService.updateUserEmailAddress(userId, newEmailAddress);
    }

}
