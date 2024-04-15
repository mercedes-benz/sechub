// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobInfoForUserService;
import com.mercedesbenz.sechub.sharedkernel.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfigurationValidator;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseUserListsJobsForProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserApprovesJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserChecksJobStatus;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserCreatesNewJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserStartsSynchronousScanByClient;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsBinaries;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsSourceCode;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * The rest api for job scheduling. It shall be same obvious like
 * https://developer.github.com/v3/issues/labels/
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(APIConstants.API_PROJECT + "{projectId}") // API like https://developer.github.com/v3/issues/labels/#create-a-label
@RolesAllowed({ RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN })
public class SchedulerRestController {

    public static final String DEFAULT_JOB_INFORMATION_SIZE = "1";
    public static final String DEFAULT_JOB_INFORMATION_PAGE = "0";
    public static final String DEFAULT_WITH_METADATA = "false";

    @Autowired
    private SchedulerApproveJobService approveJobService;

    @Autowired
    private SchedulerCreateJobService createJobService;

    @Autowired
    private SchedulerSourcecodeUploadService sourcecodeUploadService;

    @Autowired
    private SchedulerBinariesUploadService binariesUploadService;

    @Autowired
    private SchedulerGetJobStatusService jobStatusService;

    @Autowired
    private SecHubConfigurationValidator validator;

    @Autowired
    private SecHubJobInfoForUserService jobInformationService;

    @Validated
    @RolesAllowed(RoleConstants.ROLE_USER)
    @UseCaseUserStartsSynchronousScanByClient(@Step(number = 1, next = { 2, 3 }, name = "create new job"))
    @UseCaseUserCreatesNewJob(@Step(number = 1, name = "Authenticated REST call", needsRestDoc = true))
    @RequestMapping(path = "/job", method = RequestMethod.POST)
    public SchedulerResult createJob(@PathVariable("projectId") String projectId, @RequestBody @Valid SecHubConfiguration configuration) {
        return createJobService.createJob(projectId, configuration);
    }

    /* @formatter:off */
    /* FIXME Albert Tregnaghi, 2024-04-15: remove when not necessary! */
    @Deprecated // we try string
	@Validated
	@RolesAllowed(RoleConstants.ROLE_USER)
	@UseCaseUserStartsSynchronousScanByClient(@Step(number=2, name="upload sourcecode"))
	@UseCaseUserUploadsSourceCode(@Step(number=1,name="Authenticated REST call",needsRestDoc=true))
	@RequestMapping(path = "/job/{jobUUID}/sourcecodeX", method = RequestMethod.POST)
	public void uploadSourceCodeX(
				@PathVariable("projectId") String projectId,
				@PathVariable("jobUUID") UUID jobUUID,
				@RequestParam("file") MultipartFile file,
				@RequestParam("checkSum") MultipartFile checkSum
			) {
		sourcecodeUploadService.uploadSourceCode(projectId, jobUUID, file, checkSum);
	}
	/* @formatter:on */
    /* @formatter:off */
	@Validated
	@RolesAllowed(RoleConstants.ROLE_USER)
	@UseCaseUserStartsSynchronousScanByClient(@Step(number=2, name="upload sourcecode"))
	@UseCaseUserUploadsSourceCode(@Step(number=1,name="Authenticated REST call",needsRestDoc=true))
	@RequestMapping(path = "/job/{jobUUID}/sourcecode", method = RequestMethod.POST)
	public void uploadSourceCode(
	        @PathVariable("projectId") String projectId,
	        @PathVariable("jobUUID") UUID jobUUID,
	        @RequestParam("file") MultipartFile file,
	        @RequestParam("checkSum") String checkSum
	        ) {
	    sourcecodeUploadService.uploadSourceCode(projectId, jobUUID, file, checkSum);
	}
	/* @formatter:on */

    /* @formatter:off */
    @UseCaseUserStartsSynchronousScanByClient(@Step(number=2, name="upload binaries"))
    @UseCaseUserUploadsBinaries(@Step(number=1,name="Authenticated REST call" ,needsRestDoc=true))
    @RolesAllowed(RoleConstants.ROLE_USER)
    @RequestMapping(path = "/job/{jobUUID}/binaries", method = RequestMethod.POST)
    public void uploadBinaries(
    		@PathVariable("projectId") String projectId,
            @PathVariable("jobUUID") UUID jobUUID,
            HttpServletRequest request) throws Exception {
        binariesUploadService.uploadBinaries(projectId, jobUUID, request);
    }
    /* @formatter:on */

    /* @formatter:off */
	@Validated
	@RolesAllowed(RoleConstants.ROLE_USER)
	@UseCaseUserStartsSynchronousScanByClient(@Step(number=3, name="approve job"))
	@UseCaseUserApprovesJob(@Step(number=1,name="Authenticated REST call",needsRestDoc=true))
	@RequestMapping(path = "/job/{jobUUID}/approve", method = RequestMethod.PUT)
	public void approveJob(
				@PathVariable("projectId") String projectId,
				@PathVariable("jobUUID") UUID jobUUID) {
		approveJobService.approveJob(projectId,jobUUID);
	}
	/* @formatter:on */

    /* @formatter:off */
	@Validated
	@UseCaseUserStartsSynchronousScanByClient(@Step(number=4, name="get job status"))
	@UseCaseUserChecksJobStatus(@Step(number=1,name="Authenticated REST call",needsRestDoc=true))
	@RequestMapping(path = "/job/{jobUUID}", method = RequestMethod.GET)
	public ScheduleJobStatus getJobStatus(
			@PathVariable("projectId") String projectId,
			@PathVariable("jobUUID") UUID jobUUID
			) {
		/* @formatter:on */
        return jobStatusService.getJobStatus(projectId, jobUUID);

    }

    /* @formatter:off */
    @Validated
    @UseCaseUserListsJobsForProject(@Step(number=1, name="get pageable list of jobs in project", needsRestDoc=true))
    @RequestMapping(path = "/jobs", method = RequestMethod.GET)
    public SecHubJobInfoForUserListPage listJobsForProject(
            @PathVariable("projectId") String projectId,
            @RequestParam(defaultValue = DEFAULT_JOB_INFORMATION_SIZE, name = "size") int size,
            @RequestParam(defaultValue = DEFAULT_JOB_INFORMATION_PAGE, name = "page") int page,
            @RequestParam(defaultValue = DEFAULT_WITH_METADATA, name = "withMetaData") boolean withMetaData,
            @RequestParam Map<String,String> allParams
            ) {
        /* @formatter:on */
        return jobInformationService.listJobsForProject(projectId, size, page, withMetaData, allParams);

    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(validator);
    }
}
