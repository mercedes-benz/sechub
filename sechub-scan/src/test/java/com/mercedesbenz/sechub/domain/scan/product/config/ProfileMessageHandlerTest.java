// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

class ProfileMessageHandlerTest {

    private static final String PROFILE32 = "profile3";
    private static final String TEST_PROJECT_ID = "projectId";
    private static final String PROFILE3 = PROFILE32;
    private static final String PROFILE2 = "profile2";
    private static final String PROFILE1 = "profile1";

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
        assertThat(response.getMessageId()).isEqualTo(MessageID.RESULT_ENABLED_PROFILE_IDS_FOR_PROJECTS);
        Map<String, List<String>> projectToProfiles = response.get(PROJECT_ASSIGNED_AND_ENABLED_PROFILE_IDS);
        assertThat(projectToProfiles).containsKey(TEST_PROJECT_ID);
        assertThat(projectToProfiles.get(TEST_PROJECT_ID)).isEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(ActiveProfileIdsProvider.class)
    void profiles_assigned_to_project_only_enabled_projects_are_returned_in_response(String variant, List<String> allAssignedProfileIds,
            List<String> enableddProfileIds, List<String> expectedProfileIds) {
        /* prepare */
        DomainMessage message = prepareValidDomainMessage();
        List<ProductExecutionProfile> executionProfilesOfProject = createProductExecutionProfilesbyIds(allAssignedProfileIds, enableddProfileIds);
        when(productExecutionProfileRepository.findExecutionProfilesForProject(TEST_PROJECT_ID)).thenReturn(executionProfilesOfProject);

        /* execute */
        DomainMessageSynchronousResult response = handlerToTest.receiveSynchronMessage(message);

        /* test */
        assertThat(response.getMessageId()).isEqualTo(MessageID.RESULT_ENABLED_PROFILE_IDS_FOR_PROJECTS);
        Map<String, List<String>> projectToProfiles = response.get(PROJECT_ASSIGNED_AND_ENABLED_PROFILE_IDS);
        assertThat(projectToProfiles).containsKey(TEST_PROJECT_ID);
        assertThat(projectToProfiles.get(TEST_PROJECT_ID)).containsAll(expectedProfileIds).hasSize(expectedProfileIds.size());
    }

    private DomainMessage prepareValidDomainMessage() {
        DomainMessage message = new DomainMessage(MessageID.REQUEST_ENABLED_PROFILE_IDS_FOR_PROJECTS);
        message.set(PROJECT_IDS, List.of(TEST_PROJECT_ID));
        return message;
    }

    private List<ProductExecutionProfile> createProductExecutionProfilesbyIds(List<String> allProjectProfileIds, List<String> enabledProfileIdsList) {
        List<ProductExecutionProfile> executionProfilesOfProject = new ArrayList<>(allProjectProfileIds.size());
        for (String profileId : allProjectProfileIds) {
            ProductExecutionProfile profile = new ProductExecutionProfile();
            profile.id = profileId;
            if (enabledProfileIdsList.contains(profileId)) {
                profile.enabled = Boolean.TRUE;
            }
            executionProfilesOfProject.add(profile);
        }
        return executionProfilesOfProject;
    }

    private static class ActiveProfileIdsProvider implements ArgumentsProvider {

        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    /* Arguments: String variant, List<String> allAssignedProfileIds, List<String> enableddProfileIds, List<String> expectedProfileIds */
                    Arguments.of("a1",List.of(PROFILE1, PROFILE3, PROFILE2), List.of(PROFILE1,PROFILE2,PROFILE3), List.of(PROFILE1,PROFILE2,PROFILE3)),
                    Arguments.of("a2",List.of(PROFILE1, PROFILE3, PROFILE2), List.of(PROFILE1,PROFILE3), List.of(PROFILE1,PROFILE3)),
                    Arguments.of("a3",List.of(PROFILE1, PROFILE3, PROFILE2), List.of(PROFILE3,PROFILE2),List.of(PROFILE3,PROFILE2)),
                    Arguments.of("a4",List.of(PROFILE1, PROFILE3, PROFILE2), List.of(PROFILE3), List.of(PROFILE3)),
                    Arguments.of("a6",List.of(PROFILE1, PROFILE3, PROFILE2), List.of(), List.of()),
                    Arguments.of("u1",List.of(PROFILE1, PROFILE2), List.of(PROFILE1,"unknown"), List.of(PROFILE1))
            );
        }
        /* @formatter:on*/
    }
}
