// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.test.RestDocPathParameter.JOB_UUID;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.PROJECT_ID;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccess;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccessRepository;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobInfoForUserService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfigurationValidator;
import com.mercedesbenz.sechub.test.TestPortProvider;

import jakarta.validation.ValidationException;

@WebMvcTest(SchedulerRestController.class)
@ContextConfiguration(classes = { SchedulerRestController.class, SchedulerRestControllerMockTest.SimpleTestConfiguration.class })
@WithMockUser
@ActiveProfiles(Profiles.TEST)
public class SchedulerRestControllerMockTest {

    private static final String PROJECT1_ID = "project1";

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchedulerApproveJobService mockedScheduleService;

    @MockBean
    private SchedulerCreateJobService mockedScheduleCreateJobService;

    @MockBean
    private SchedulerGetJobStatusService mockedScheduleJobStatusService;

    @MockBean
    private SchedulerSourcecodeUploadService mockedSourcecodeUploadService;

    @MockBean
    private SchedulerBinariesUploadService mockedBinariesUploadService;

    @MockBean
    private SecHubConfigurationValidator sechubConfigurationValidator;

    @MockBean
    private SecHubJobRepository mockedJobRepository; // even when not used here, its necessary to define the mock-its
                                                     // indirectly used by scheduler service auto wiraing ?!?
    @MockBean
    private ScheduleAccessRepository mockedProjectRepository;

    @MockBean
    private SecHubJobInfoForUserService jobInfoForUserService;

    private ScheduleAccess project1;

    private UUID randomUUID;

    @Test
    public void get_job_status_from_existing_job_returns_information() throws Exception {
        /* prepare */

        ScheduleJobStatus status = new ScheduleJobStatus();
        status.jobUUID = randomUUID;
        status.result = ExecutionResult.NONE.name();
        status.state = ExecutionState.STARTED.name();
        status.trafficLight = null;

        when(mockedScheduleJobStatusService.getJobStatus(PROJECT1_ID, randomUUID)).thenReturn(status);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildGetJobStatusUrl(PROJECT1_ID,randomUUID.toString())).
        			contentType(MediaType.APPLICATION_JSON_VALUE)
        		).
        			andExpect(status().isOk()).
        			andExpect(content().json("{jobUUID:"+randomUUID.toString()+", result:NONE, state:STARTED, trafficLight:null}")
        		);

        /* @formatter:on */
    }

    @Test
    public void scheduling__returns_job_id_from_service() throws Exception {
        /* prepare */
        UUID randomUUID = UUID.randomUUID();
        SchedulerResult mockResult = new SchedulerResult(randomUUID);

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildAddJobUrl(PROJECT1_ID)).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content("{}")
        		).
        			andExpect(status().isOk()).
        			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")
        		);

        /* @formatter:on */
    }

    @Test
    public void scheduling_calls_always_validator() throws Exception {
        /* prepare */
        UUID randomUUID = UUID.randomUUID();
        SchedulerResult mockResult = new SchedulerResult(randomUUID);

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildAddJobUrl(PROJECT1_ID)).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content("{}")
        		).
        			andExpect(status().isOk()).
        			andExpect(content().json("{jobId:"+randomUUID.toString()+"}")
        		);

        verify(this.sechubConfigurationValidator).validate(any(),any());

        /* @formatter:on */
    }

    @Test
    public void uploadSourceCode_calls_uploadservice_with_given_checksum() throws Exception {
        /* prepare */

        ScheduleSecHubJob job = new ScheduleSecHubJob() {
            public UUID getUUID() {
                return randomUUID;
            };
        };
        job.setExecutionResult(ExecutionResult.OK);
        job.setStarted(LocalDateTime.now().minusMinutes(15));
        job.setEnded(LocalDateTime.now());
        job.setExecutionState(ExecutionState.INITIALIZING);
        job.setOwner("CREATOR1");
        job.setTrafficLight(TrafficLight.GREEN);

        ScheduleJobStatus status = new ScheduleJobStatus(job);

        when(mockedScheduleJobStatusService.getJobStatus(PROJECT1_ID, randomUUID)).thenReturn(status);

        InputStream inputStreamTo = ScheduleTestFileSupport.getTestfileSupport().getInputStreamTo("upload/zipfile_contains_only_test1.txt.zip");
        MockMultipartFile file1 = new MockMultipartFile("file", inputStreamTo);
        MockMultipartFile checkSum = new MockMultipartFile("checkSum", "", "", "myChecksum".getBytes());

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		multipart(https(PORT_USED).
        		    buildUploadSourceCodeUrl(PROJECT_ID.pathElement(), JOB_UUID.pathElement()), PROJECT1_ID, randomUUID).
        			file(file1).
        			file(checkSum)
        		);

        verify(mockedSourcecodeUploadService).uploadSourceCode(PROJECT1_ID, randomUUID, file1, checkSum);
        /* @formatter:on */
    }

    @Test
    public void when_scheduler_throws_an_validation_exception_a_HTTP_400_bad_request_is_thrown() throws Exception {
        /* prepare */
        when(mockedScheduleCreateJobService.createJob(any(), any())).thenThrow(new ValidationException("something-goes-wrong"));

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildAddJobUrl(PROJECT1_ID)).
        			contentType(MediaType.APPLICATION_JSON_VALUE)
        		)./*andDo(print()). */
        			andExpect(status().isBadRequest());

        /* @formatter:on */
    }

    @Test
    public void scheduling_a_sechub_configuration_having_no_api_version_set__fails_HTTP_400_bad_request() throws Exception {
        /* prepare */
        SchedulerResult mockResult = Mockito.mock(SchedulerResult.class);

        when(mockedScheduleCreateJobService.createJob(any(), any(SecHubConfiguration.class))).thenReturn(mockResult);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildAddJobUrl(PROJECT1_ID)).
        			contentType(MediaType.APPLICATION_JSON_VALUE)
        		).andDo(print()).
        			andExpect(status().isBadRequest());

        /* @formatter:on */
    }

    @BeforeEach
    public void beforeEach() {
        randomUUID = UUID.randomUUID();
        project1 = mock(ScheduleAccess.class);

        ProjectAccessCompositeKey key = new ProjectAccessCompositeKey("user", PROJECT1_ID);
        when(project1.getKey()).thenReturn(key);

        when(mockedProjectRepository.findById(key)).thenReturn(Optional.of(project1));

        when(sechubConfigurationValidator.supports(SecHubConfiguration.class)).thenReturn(true);
    }

    @TestConfiguration
    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }
}
