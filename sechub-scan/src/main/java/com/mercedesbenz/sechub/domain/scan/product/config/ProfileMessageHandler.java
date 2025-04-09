// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.PROJECT_ASSIGNED_PROFILE_IDS;
import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.PROJECT_IDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SynchronMessageHandler;

@Component
public class ProfileMessageHandler implements SynchronMessageHandler {

    private final ProductExecutionProfileRepository productExecutionProfileRepository;

    public ProfileMessageHandler(ProductExecutionProfileRepository productExecutionProfileRepository) {
        this.productExecutionProfileRepository = productExecutionProfileRepository;
    }

    @IsRecevingSyncMessage(MessageID.REQUEST_PROFILE_IDS_FOR_PROJECT)
    @IsSendingSyncMessageAnswer(value = MessageID.RESULT_PROFILE_IDS_FOR_PROJECT, answeringTo = MessageID.REQUEST_PROFILE_IDS_FOR_PROJECT, branchName = "success")
    @Override
    public DomainMessageSynchronousResult receiveSynchronMessage(DomainMessage request) {
        List<String> projectIds = request.get(PROJECT_IDS);

        DomainMessageSynchronousResult response = new DomainMessageSynchronousResult(MessageID.RESULT_PROFILE_IDS_FOR_PROJECT);
        Map<String, List<String>> projectToProfileIds = new HashMap<>();
        for (String id : projectIds) {
            List<ProductExecutionProfile> executionProfilesForProject = productExecutionProfileRepository.findExecutionProfilesForProject(id);
            /* @formatter:off */
            List<String> profileIds = executionProfilesForProject.stream()
                                                                .map(ProductExecutionProfile::getId)
                                                                .collect(Collectors.toList());
            /* @formatter:on */
            projectToProfileIds.put(id, profileIds);
        }
        response.set(PROJECT_ASSIGNED_PROFILE_IDS, projectToProfileIds);
        return response;
    }

}
