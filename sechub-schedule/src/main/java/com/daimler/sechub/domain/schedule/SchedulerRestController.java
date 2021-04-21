// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

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

import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfigurationValidator;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserApprovesJob;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserChecksJobStatus;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserCreatesNewJob;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserStartsSynchronousScanByClient;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsSourceCode;

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

    @Autowired
    private SchedulerApproveJobService approveJobService;

    @Autowired
    private SchedulerCreateJobService createJobService;

    @Autowired
    private SchedulerUploadService uploadService;

    @Autowired
    private SchedulerGetJobStatusService jobStatusService;

    @Autowired
    private SecHubConfigurationValidator validator;

    @Validated
    @RolesAllowed(RoleConstants.ROLE_USER)
    @UseCaseUserStartsSynchronousScanByClient(@Step(number = 1, next = { 2, 3 }, name = "create new job"))
    @UseCaseUserCreatesNewJob(@Step(number = 1, name = "Authenticated REST call", needsRestDoc = true))
    @RequestMapping(path = "/job", method = RequestMethod.POST)
    public SchedulerResult createJob(@PathVariable("projectId") String projectId, @RequestBody @Valid SecHubConfiguration configuration) {
        return createJobService.createJob(projectId, configuration);
    }

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
		uploadService.uploadSourceCode(projectId, jobUUID, file, checkSum);
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

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(validator);
    }
}
