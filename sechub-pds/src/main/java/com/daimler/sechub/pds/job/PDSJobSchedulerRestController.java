// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.sechub.pds.PDSAPIConstants;
import com.daimler.sechub.pds.PDSRoleConstants;

/**
 * The REST API for PDS job scheduling
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(PDSAPIConstants.API_JOB)
@RolesAllowed({PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN})
public class PDSJobSchedulerRestController {

	@Autowired
	private PDSApproveJobService approveJobService;

	@Autowired
	private PDSCreateJobService createJobService;

	@Autowired
	private PDSFileUploadService uploadService;

	@Autowired
	private PDSGetJobStatusService jobStatusService;

	@Validated
	@RolesAllowed(PDSRoleConstants.ROLE_USER)
	@RequestMapping(method = RequestMethod.POST)
	public PDSJobCreateResult createJob(
			@RequestBody PDSConfiguration configuration) {
		return createJobService.createJob(configuration);
	}


	/* @formatter:off */
	@Validated
	@RolesAllowed(PDSRoleConstants.ROLE_USER)
	@RequestMapping(path = "/{jobUUID}/upload", method = RequestMethod.POST)
	public void uploadData(
				@PathVariable("jobUUID") UUID jobUUID,
				@RequestParam("file") MultipartFile file,
				@RequestParam("checkSum") String checkSum
			) {
		uploadService.upload(jobUUID,file,checkSum);
	}
	/* @formatter:on */


	/* @formatter:off */
	@Validated
	@RolesAllowed(PDSRoleConstants.ROLE_USER)
	@RequestMapping(path = "/{jobUUID}/approve", method = RequestMethod.PUT)
	public void approveJob(
				@PathVariable("jobUUID") UUID jobUUID) {
		approveJobService.approveJob(jobUUID);
	}
	/* @formatter:on */


	/* @formatter:off */
	@Validated
	@RequestMapping(path = "/{jobUUID}", method = RequestMethod.GET)
	public PDSJobStatus getJobStatus(
			@PathVariable("jobUUID") UUID jobUUID
			) {
		/* @formatter:on */
		return jobStatusService.getJobStatus(jobUUID);

	}

}
