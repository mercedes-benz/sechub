// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.PROJECT_ASSIGNED_PROFILE_IDS;
import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.PROJECT_IDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

class ProfileMessageHandlerTest {

    private static final String TEST_PROJECT_ID = "projectId";

    private ProfileMessageHandler handlerToTest;

    private static final ProductExecutionProfileRepository productExecutionProfileRepository = mock();

    @BeforeEach
    void beforeEach() {
        Mockito.reset(productExecutionProfileRepository);
        handlerToTest = new ProfileMessageHandler(productExecutionProfileRepository);
    }

    @Test
    void no_profiles_assigned_results_in_empty_profiles_list_in_response() {
        /* prepare */
        DomainMessage message = prepareValidDomainMessage();
        when(productExecutionProfileRepository.findExecutionProfilesForProject(TEST_PROJECT_ID)).thenReturn(Collections.emptyList());

        /* execute */
        DomainMessageSynchronousResult response = handlerToTest.receiveSynchronMessage(message);

        /* test */
        Map<String, List<String>> projectToProfiles = response.get(PROJECT_ASSIGNED_PROFILE_IDS);
        assertThat(projectToProfiles).containsKey(TEST_PROJECT_ID);
        assertThat(projectToProfiles.get(TEST_PROJECT_ID)).isEmpty();
    }

    @Test
    void profiles_assigned_results_in_profiles_list_in_response() {
        /* prepare */
        List<String> expectedProfileIds = List.of("profile1", "profile3", "profile8");
        DomainMessage message = prepareValidDomainMessage();
        List<ProductExecutionProfile> executionProfilesOfProject = createProductExecutionProfilesbyIds(expectedProfileIds);
        when(productExecutionProfileRepository.findExecutionProfilesForProject(TEST_PROJECT_ID)).thenReturn(executionProfilesOfProject);

        /* execute */
        DomainMessageSynchronousResult response = handlerToTest.receiveSynchronMessage(message);

        /* test */
        Map<String, List<String>> projectToProfiles = response.get(PROJECT_ASSIGNED_PROFILE_IDS);
        assertThat(projectToProfiles).containsKey(TEST_PROJECT_ID);
        assertThat(projectToProfiles.get(TEST_PROJECT_ID)).containsAll(expectedProfileIds);
    }

    private DomainMessage prepareValidDomainMessage() {
        DomainMessage message = new DomainMessage(MessageID.REQUEST_PROFILE_IDS_FOR_PROJECT);
        message.set(PROJECT_ASSIGNED_PROFILE_IDS, null);
        message.set(PROJECT_IDS, List.of(TEST_PROJECT_ID));
        return message;
    }

    private List<ProductExecutionProfile> createProductExecutionProfilesbyIds(List<String> expectedProfileIds) {
        List<ProductExecutionProfile> executionProfilesOfProject = new ArrayList<>(expectedProfileIds.size());
        for (String profileId : expectedProfileIds) {
            ProductExecutionProfile profile = new ProductExecutionProfile();
            profile.id = profileId;
            executionProfilesOfProject.add(profile);
        }
        return executionProfilesOfProject;
    }

}
