// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatus;
import com.mercedesbenz.sechub.pds.PDSAPIConstants;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserCreatesJob;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserFetchesJobMessages;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserFetchesJobResult;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserFetchesJobStatus;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserMarksJobReadyToStart;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserRequestsJobCancellation;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserUploadsJobData;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The REST API for PDS jobs
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(PDSAPIConstants.API_JOB)
@RolesAllowed({ PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN })
public class PDSJobRestController {

    @Autowired
    private PDSJobTransactionService updateJobTransactionService;

    @Autowired
    private PDSCreateJobService createJobService;

    @Autowired
    private PDSFileUploadJobService fileUploadJobService;

    @Autowired
    private PDSGetJobStatusService jobStatusService;

    @Autowired
    private PDSGetJobResultService jobResultService;

    @Autowired
    private PDSGetJobMessagesService jobMessagesService;

    @Autowired
    private PDSRequestJobCancellationService requestJobCancellationService;

    @Validated
    @RequestMapping(path = "create", method = RequestMethod.POST)
    @UseCaseUserCreatesJob(@PDSStep(name = "rest call", description = "User creates job. If configuration is not valid an error will be thrown", number = 1))
    public PDSJobCreateResult createJob(@RequestBody PDSJobConfiguration configuration) {
        return createJobService.createJob(configuration);
    }

    /* @formatter:off */
	@RequestMapping(path = "{jobUUID}/upload/{fileName}", method = RequestMethod.POST)
	@UseCaseUserUploadsJobData(@PDSStep(name="rest call",description = "User uploads a file to workspace of given job",number=1))
	public void upload(
				@PathVariable("jobUUID") UUID jobUUID,
				@PathVariable("fileName") String fileName,
				HttpServletRequest request
			) {
		fileUploadJobService.upload(jobUUID,fileName,request);
	}
	/* @formatter:on */

    /* @formatter:off */
	@Validated
	@RequestMapping(path = "{jobUUID}/mark-ready-to-start", method = RequestMethod.PUT)
	@UseCaseUserMarksJobReadyToStart(@PDSStep(name="rest call",description = "User marks job as ready to start.",number=1))
	public void markReadyToStart(
				@PathVariable("jobUUID") UUID jobUUID) {
		updateJobTransactionService.markReadyToStartInOwnTransaction(jobUUID);
	}
	/* @formatter:on */

    /* @formatter:off */
    @Validated
    @RequestMapping(path = "{jobUUID}/cancel", method = RequestMethod.PUT)
    @UseCaseUserRequestsJobCancellation(@PDSStep(name="rest call",description = "User cancels a job",number=1))
    public void cancelJob(
                @PathVariable("jobUUID") UUID jobUUID) {
        requestJobCancellationService.requestJobCancellation(jobUUID);
    }
    /* @formatter:on */

    /* @formatter:off */
	@Validated
	@RequestMapping(path = "{jobUUID}/status", method = RequestMethod.GET)
	@UseCaseUserFetchesJobStatus(@PDSStep(name="rest call",description = "User fetches status of a job.",number=1))
	public PDSJobStatus getJobStatus(
			@PathVariable("jobUUID") UUID jobUUID
			) {
		/* @formatter:on */
        return jobStatusService.getJobStatus(jobUUID);

    }

    /* @formatter:off */
    @Validated
    @RequestMapping(path = "{jobUUID}/result", method = RequestMethod.GET)
    @UseCaseUserFetchesJobResult(@PDSStep(name="rest call",description = "User wants to get result of a job",number=1))
    public String getJobResult(
            @PathVariable("jobUUID") UUID jobUUID
            ) {
        /* @formatter:on */
        return jobResultService.getJobResult(jobUUID);
    }

    /* @formatter:off */
    @Validated
    @RequestMapping(path = "{jobUUID}/messages", method = RequestMethod.GET)
    @UseCaseUserFetchesJobMessages(@PDSStep(name="rest call",description = "User wants to get messages of job",number=1))
    public String getJobMessages(
            @PathVariable("jobUUID") UUID jobUUID
            ) {
        /* @formatter:on */
        return jobMessagesService.getJobMessages(jobUUID);
    }

}
