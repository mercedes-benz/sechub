// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.mapping;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.domain.administration.AdministrationAPIConstants;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesMappingConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesMappingConfiguration;

/**
 * The rest API for mapping administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
public class MappingAdministrationRestController {

	@Autowired
	FetchMappingService fetchMappingService;
	
	@Autowired
    UpdateMappingService updateMappingService;

	/* @formatter:off */
	@UseCaseAdminFetchesMappingConfiguration(@Step(number=1,name="Rest call",description="Administrator wants to fetch a mapping configuration",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_CONFIG_MAPPING, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public MappingData fetchMappingData(@PathVariable(name="mappingId") String mappingId) {
		/* @formatter:on */
		return fetchMappingService.fetchMappingData(mappingId);
	}
	
	/* @formatter:off */
    @UseCaseAdministratorUpdatesMappingConfiguration(@Step(number=1,name="Rest call",description="Administrator wants to update a mapping configuration",needsRestDoc=true))
    @RequestMapping(path = AdministrationAPIConstants.API_CONFIG_MAPPING, method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public void updateMapping(@PathVariable(name="mappingId") String mappingId, @RequestBody MappingData mappingData) {
        /* @formatter:on */
        updateMappingService.updateMapping(mappingId, mappingData);
    }



}