// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant;

import java.util.UUID;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.assistant.UseCaseUserRequestsFindingExplanation;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;

import jakarta.annotation.security.RolesAllowed;

@RestController
@EnableAutoConfiguration
@RequestMapping(APIConstants.API_ASSISTANT)
@RolesAllowed({ RoleConstants.ROLE_USER, RoleConstants.ROLE_OWNER, RoleConstants.ROLE_SUPERADMIN })
public class AssistantRestController {
    
    private final FindingAssistantService findingAssistantService;

    /* @formatter:off */
    public AssistantRestController(FindingAssistantService findingAssistantService) {
        this.findingAssistantService = findingAssistantService;
        /* @formatter:on */
    }
    
    /* @formatter:off */
    @UseCaseUserRequestsFindingExplanation(@Step(number=1, name="REST API call to get an explanation for the given finding.", needsRestDoc=true))
    @GetMapping(path = "/explanation/project/{projectId}/job/{jobUUID}/finding/{findingId}", produces= {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public SecHubExplanationResponse explainFinding(
            @PathVariable("projectId") String projectId,
            @PathVariable("jobUUID") UUID jobUUID,
            @PathVariable("findingId") Integer findingId
            ) {
        /* @formatter:on */
        return findingAssistantService.createSecHubExplanationResponse(projectId, jobUUID, findingId);
    }

}
