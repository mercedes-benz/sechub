// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.sharedkernel.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserFetchesFalsePositiveConfigurationOfProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserMarksFalsePositives;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUnmarksFalsePositiveProjectData;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUnmarksFalsePositives;

import jakarta.annotation.security.RolesAllowed;

/**
 * The rest API for project false positive handling - API centric
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(APIConstants.API_PROJECT + "{projectId}")
@RolesAllowed({ RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN })
public class FalsePositiveRestController {

    @Autowired
    private FalsePositiveDataService falsePositiveDataService;

    /* @formatter:off */
	@UseCaseUserMarksFalsePositives(@Step(number=1,name="REST API call to define false positives by JSON data containing identifiers for existing jobs or false positive project data",needsRestDoc=true))
	@RequestMapping(path = "/false-positives", method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
    public void addFalsePositiveData(
            @PathVariable("projectId") String projectId,
            @RequestBody FalsePositiveDataList data
            ) {
        /* @formatter:on */
        falsePositiveDataService.addFalsePositives(projectId, data);

    }

    /* @formatter:off */
    @UseCaseUserUnmarksFalsePositives(@Step(number=1,name="REST API call to remove existing false positive definition",needsRestDoc=true))
    @RequestMapping(path = "/false-positive/{jobUUID}/{findingId}", method = RequestMethod.DELETE)
    public void removeFalsePositiveFromProjectByJobUUIDAndFindingId(
            @PathVariable("projectId") String projectId,
            @PathVariable("jobUUID") UUID jobUUID,
            @PathVariable("findingId") int findingId
            ) {
        /* @formatter:on */
        falsePositiveDataService.removeFalsePositive(projectId, jobUUID, findingId);

    }

    /* @formatter:off */
    @UseCaseUserUnmarksFalsePositiveProjectData(@Step(number=1,name="REST API call to remove existing false positive project data definition by id",needsRestDoc=true))
    @RequestMapping(path = "/false-positive/project-data/{id}", method = RequestMethod.DELETE)
    public void removeFalsePositiveFromProjectByProjectDataId(
            @PathVariable("projectId") String projectId,
            @PathVariable("id") String id
            ) {
        /* @formatter:on */
        falsePositiveDataService.removeFalsePositiveByProjectDataId(projectId, id);

    }

    /* @formatter:off */
    @UseCaseUserFetchesFalsePositiveConfigurationOfProject(@Step(number=1,name="REST API call to fetch existing false positive configuration of project",needsRestDoc=true))
    @RequestMapping(path = "/false-positives", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
    public FalsePositiveProjectConfiguration fetchFalsePositivesProjectConfiguration(
            @PathVariable("projectId") String projectId
            ) {
        /* @formatter:on */
        return falsePositiveDataService.fetchFalsePositivesProjectConfiguration(projectId);

    }

}
