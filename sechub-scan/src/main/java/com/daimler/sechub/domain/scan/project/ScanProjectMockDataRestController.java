// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.user.UseCaseUserDefinesProjectMockdata;
import com.daimler.sechub.sharedkernel.usecases.user.UseCaseUserRetrievesProjectMockdata;

/**
 * The rest API for project mockdata 
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(APIConstants.API_PROJECT+"{projectId}") 
@RolesAllowed({RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN})
@Profile(Profiles.MOCKED_PRODUCTS)
public class ScanProjectMockDataRestController {

	@Autowired
	private ScanProjectMockDataConfigurationService configurationService;


	/* @formatter:off */
	@UseCaseUserRetrievesProjectMockdata(@Step(number=1,name="REST API call to get JSON data",needsRestDoc=true))
	@RequestMapping(path = "/mockdata", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public ScanProjectMockDataConfiguration retrieveProjectMockDataConfiguration(
			@PathVariable("projectId") String projectId
			) {
		/* @formatter:on */
		return configurationService.retrieveProjectMockDataConfiguration(projectId);

	}
	
	/* @formatter:off */
	@UseCaseUserDefinesProjectMockdata(@Step(number=1,name="REST API call to define mock configuration byJSON data",needsRestDoc=true))
	@RequestMapping(path = "/mockdata", method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
	public void defineProjectMockDataConfiguration(
			@PathVariable("projectId") String projectId,
			@RequestBody ScanProjectMockDataConfiguration configuration
			) {
		
		configurationService.defineProjectMockDataConfiguration(projectId,configuration);
		/* @formatter:on */

	}

}
