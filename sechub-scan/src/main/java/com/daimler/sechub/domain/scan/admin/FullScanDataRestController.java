// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logforgery.LogSanitizer;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorDownloadsFullScanDataForJob;

@RestController
@EnableAutoConfiguration
@RequestMapping(APIConstants.API_ADMINISTRATION)
@RolesAllowed({RoleConstants.ROLE_SUPERADMIN})
public class FullScanDataRestController {

	private static final Logger LOG = LoggerFactory.getLogger(FullScanDataRestController.class);

	@Autowired
	FullScanDataService fullScanDataService;

	@Autowired
	AuditLogService auditLogService;

	@Autowired
	LogSanitizer logSanitizer;

	/* @formatter:off */
	@UseCaseAdministratorDownloadsFullScanDataForJob(@Step(number=1,next=2,name="REST API call to zip file containing full scan data",needsRestDoc=true))
	@RequestMapping(path = "/scan/download/{sechubJobUUID}", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public void getFullScanZipFileForJob(
			@PathVariable("sechubJobUUID") UUID sechubJobUUID, HttpServletResponse response
			) {
		/* @formatter:on */
		auditLogService.log("Starts downloading full scan logs for sechub job {}", logSanitizer.sanitize(sechubJobUUID,-1));

	    response.setContentType("application/zip");
	    response.setHeader("Content-Disposition", "attachment; filename=full_scandata_"+sechubJobUUID.toString()+".zip");

	    FullScanData fullScanData = fullScanDataService.getFullScanData(sechubJobUUID);

	    try (OutputStream outputStream= response.getOutputStream()) {
			FullScanDataToZipOutputSupport support = new FullScanDataToZipOutputSupport();
			support.writeScanData(fullScanData, outputStream);
		} catch (IOException e) {
			LOG.error("Was not able to provide zip file for full scan data of {}",logSanitizer.sanitize(sechubJobUUID,-1), e);
			throw new NotFoundException("Was not able to support zip file, see logs for details");
		}
	}




}
