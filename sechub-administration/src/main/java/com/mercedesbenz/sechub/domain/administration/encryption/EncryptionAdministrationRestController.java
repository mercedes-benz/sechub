// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.encryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionDataValidator;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminFetchesEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminStartsEncryptionRotation;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;

/**
 * The rest api for encryption done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile({ Profiles.TEST, Profiles.ADMIN_ACCESS })
public class EncryptionAdministrationRestController {

    @Autowired
    AdministrationEncryptionRotationService administrationEncryptionRotationService;

    @Autowired
    AdministrationEncryptionStatusService administrationStatusService;

    @Autowired
    SecHubEncryptionDataValidator encryptionDataValidator;

    /* @formatter:off */
	@UseCaseAdminStartsEncryptionRotation(@Step(number=1,name="Rest call",description="Admin triggers rotation of encryption via REST", needsRestDoc =true))
	@RequestMapping(path = AdministrationAPIConstants.API_ADMIN_ENCRYPTION_ROTATION, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	public void rotateEncryption(@RequestBody @Valid SecHubEncryptionData data) {
		/* @formatter:on */
        administrationEncryptionRotationService.rotateEncryption(data);
    }

    /* @formatter:off */
	@UseCaseAdminFetchesEncryptionStatus(@Step(number=1,name="Rest call",description="Admin fetches encryption status from domains via REST", needsRestDoc =true))
	@RequestMapping(path = AdministrationAPIConstants.API_ADMIN_ENCRYPTION_STATUS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public SecHubEncryptionStatus fetchEncryptionStatus() {
	    /* @formatter:on */
        return administrationStatusService.fetchStatus();
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(encryptionDataValidator);
    }

}
