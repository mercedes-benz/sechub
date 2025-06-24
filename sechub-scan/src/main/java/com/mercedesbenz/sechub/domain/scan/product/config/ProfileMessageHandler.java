// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.project.UseCaseGetAssignedProjectDataList;

@Component
public class ProfileMessageHandler implements SynchronMessageHandler {

    private final ProductExecutionProfileRepository productExecutionProfileRepository;

    public ProfileMessageHandler(ProductExecutionProfileRepository productExecutionProfileRepository) {
        this.productExecutionProfileRepository = productExecutionProfileRepository;
    }

    @UseCaseGetAssignedProjectDataList(@Step(number = 2, name = "Fetch enabled profiles for project", description = "Event handling at scan domain to collect enabled profiles for projects"))
    @IsRecevingSyncMessage(MessageID.REQUEST_ENABLED_PROFILE_IDS_FOR_PROJECTS)
    @IsSendingSyncMessageAnswer(value = MessageID.RESULT_ENABLED_PROFILE_IDS_FOR_PROJECTS, answeringTo = MessageID.REQUEST_ENABLED_PROFILE_IDS_FOR_PROJECTS, branchName = "success")
    @Override
    public DomainMessageSynchronousResult receiveSynchronMessage(DomainMessage request) {
        List<String> projectIds = request.get(PROJECT_IDS);

        Map<String, List<String>> projectToProfileIds = new HashMap<>();
        for (String id : projectIds) {
            List<String> profileIds = productExecutionProfileRepository.findOrderedIdsOfEnabledExecutionProfilesForProject(id);
            projectToProfileIds.put(id, profileIds);
        }

        DomainMessageSynchronousResult response = new DomainMessageSynchronousResult(MessageID.RESULT_ENABLED_PROFILE_IDS_FOR_PROJECTS);
        response.set(PROJECT_ASSIGNED_AND_ENABLED_PROFILE_IDS, projectToProfileIds);

        return response;
    }

}
