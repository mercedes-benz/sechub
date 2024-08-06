// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.domain.scan.NetworkTarget;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;
import com.mercedesbenz.sechub.domain.scan.resolve.TargetResolverService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerBinariesUploadConfiguration;
import com.mercedesbenz.sechub.domain.schedule.SchedulerCreateJobService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerGetJobStatusService;
import com.mercedesbenz.sechub.domain.schedule.SchedulerRestController;
import com.mercedesbenz.sechub.domain.schedule.SchedulerSourcecodeUploadConfiguration;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccessRepository;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionResult;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobFactory;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

/**
 * Full spring boot tests which checks multiple parts. Why so much different
 * tests inside one test class? Because here we launch a complete spring boot
 * container with full setup (means all classes are inspected) which takes a
 * long time to startup.
 *
 * The long startup time is for only for one spring boot test class setup, means
 * adding additional tests via additional methods has nearly no time impact.
 * Example: If we have 3 test classes with full spring boot setup and the full
 * spring boot test setup for one class takes 7 seconds and we create 3 separate
 * classes, we need 21 seconds for startup. If we combine it, we have need only
 * 7 seconds.
 *
 * Because we want to have faster unit tests/builds we do this here this way.
 *
 * Combined parts inside this multi spring test are:
 * <ul>
 * <li>sechub-server spring boot container can be started</li>
 * <li>some defaults are as expected</li>
 * <li>target resolver works as defaults are as expected<br>
 * <br>
 * Inside application-test.properties we have defined strategies, which will
 * treat "*.intranet.example.com/org" and "192.168.*.*" as INTRANET. <br>
 * This integration test checks if the configured values are really used</li>
 * </ul>
 *
 * @author Albert Tregnaghi
 *
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@WithMockUser(roles = { RoleConstants.ROLE_USER })
@ActiveProfiles(Profiles.TEST)
public class SecHubMultiSpringBootTest {

    private static final String PROJECT_ID = "project1";
    private SecHubConfiguration configuration;
    private ScheduleSecHubJob job;

    private UUID jobUUID;

    private String project;

    private String projectUUID = "projectId1";

    @Autowired
    private SchedulerRestController schedulerRestController;

    @Autowired
    SchedulerSourcecodeUploadConfiguration sourcecodeUploadConfiguration;

    @Autowired
    SchedulerBinariesUploadConfiguration binariesUploadConfiguration;

    @Autowired
    ScheduleEncryptionService encryptionService;

    @Autowired
    TargetResolverService targetResolverServiceToTest;

    @Autowired
    private SchedulerGetJobStatusService getJobStatusServiceToTest;

    @Autowired
    private SchedulerCreateJobService createJobServiceToTest;

    @MockBean
    private SecHubJobFactory jobFactory;

    @MockBean
    private SecHubJobRepository jobRepository;

    @MockBean
    private ScheduleAccessRepository projectUserAccessRepository;

    @MockBean
    private UserInputAssertion assertion;

    @BeforeEach
    void beforeEach() {
        jobUUID = UUID.randomUUID();
        job = mock(ScheduleSecHubJob.class);
        configuration = mock(SecHubConfiguration.class);
        project = "projectId";

        when(job.getProjectId()).thenReturn(project);
        when(jobRepository.save(job)).thenReturn(job);

        when(job.getUUID()).thenReturn(jobUUID);
        when(job.getProjectId()).thenReturn(projectUUID);
        when(jobFactory.createJob(eq(configuration))).thenReturn(job);
    }

    @Test
    void context_loads_and_some_defaults_are_as_expected() throws Exception {
        // see https://spring.io/guides/gs/testing-web/ for details about testing with
        // spring MVC test
        assertThat(schedulerRestController).isNotNull(); // we test that we got the schedulerRestController. Means - the spring container
                                                         // context
        // has been loaded successfully!

        /* check configuration defaults injected by container are as expected */
        assertTrue(sourcecodeUploadConfiguration.isZipValidationEnabled());
        assertTrue(sourcecodeUploadConfiguration.isChecksumValidationEnabled());

        assertEquals(50 * 1024 * 1024, binariesUploadConfiguration.getMaxUploadSizeInBytes());

        // test encryption service is initialized and works
        String textToEncrypt = "i need encryption";
        ScheduleEncryptionResult encryptResult = encryptionService.encryptWithLatestCipher(textToEncrypt);
        String decrypted = encryptionService.decryptToString(encryptResult.getEncryptedData(), encryptResult.getCipherPoolId(),
                encryptResult.getInitialVector());
        assertEquals(textToEncrypt, decrypted);

    }

    @Test
    void target_resolver_test_product_failure_demo_example_org__is_INTERNET() {
        /* prepare */
        URI uri = URI.create("https://productfailure.demo.example.org");

        /* execute */
        NetworkTarget found = targetResolverServiceToTest.resolveTarget(uri);

        /* test */
        assertEquals(new NetworkTarget(uri, NetworkTargetType.INTERNET), found);

    }

    @Test
    void target_resolver_test_ip_172_217_22_99__IS_INTERNET() throws Exception {
        /* prepare */
        InetAddress address = Inet4Address.getByName("172.217.22.99");

        /* execute */
        NetworkTarget found = targetResolverServiceToTest.resolveTarget(address);

        /* test */
        assertEquals(new NetworkTarget(address, NetworkTargetType.INTERNET), found);

    }

    @Test
    void target_resolver_test_something_intranet_example_org__is_INTRANET() {
        /* prepare */
        URI uri = URI.create("https://something.intranet.example.org");

        /* execute */
        NetworkTarget found = targetResolverServiceToTest.resolveTarget(uri);

        /* test */
        assertEquals(new NetworkTarget(uri, NetworkTargetType.INTRANET), found);

    }

    @Test
    void target_resolver_test_ip_192_168_22_99__IS_INTRANET() throws Exception {
        /* prepare */
        InetAddress address = Inet4Address.getByName("192.168.22.99");

        /* execute */
        NetworkTarget found = targetResolverServiceToTest.resolveTarget(address);

        /* test */
        assertEquals(new NetworkTarget(address, NetworkTargetType.INTRANET), found);

    }

    @Test
    void target_resolver_test_uri_hostname_startswith_192_IS_INTRANET() {
        /* prepare */
        URI uri = URI.create("https://192.168.22.99:7777");

        /* execute */
        NetworkTarget found = targetResolverServiceToTest.resolveTarget(uri);

        /* test */
        assertEquals(new NetworkTarget(uri, NetworkTargetType.INTRANET), found);

    }

    @Test
    void get_a_job_status_from_an_unexisting_project_throws_NOT_FOUND_exception() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        when(jobRepository.findById(jobUUID)).thenReturn(Optional.of(mock(ScheduleSecHubJob.class)));// should not be necessary, but to

        /* execute + test */
        // prevent dependency to call
        // hierachy... we simulate job can be
        // found
        Assertions.assertThrows(NotFoundException.class, () -> {
            getJobStatusServiceToTest.getJobStatus("a-project-not-existing", jobUUID);
        });
    }

    @Test
    void get_a_job_status_from_an_exsting_project_but_no_job_throws_NOT_FOUND_exception() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        when(jobRepository.findById(jobUUID)).thenReturn(Optional.empty()); // not found...

        /* execute + test */
        Assertions.assertThrows(NotFoundException.class, () -> {
            getJobStatusServiceToTest.getJobStatus(PROJECT_ID, jobUUID);
        });
    }

    @Test
    void create_job_scheduling_a_new_job_to_an_unexisting_project_throws_NOT_FOUND_exception() {
        /* execute + test */
        Assertions.assertThrows(NotFoundException.class, () -> {
            createJobServiceToTest.createJob("a-project-not-existing", configuration);
        });
    }

    @Test
    void create_job_no_access_entry__scheduling_a_configuration__will_throw_not_found_exception() {
        /* execute + test */
        Assertions.assertThrows(NotFoundException.class, () -> {
            createJobServiceToTest.createJob(PROJECT_ID, configuration);
        });
    }
}
