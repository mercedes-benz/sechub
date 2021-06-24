// SPDX-License-Identifier: MIT
package com.daimler.sechub.server.core;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logging.IntegrationTestSecurityLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.logging.SecurityLogData;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistory;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventInspectorService;
import com.daimler.sechub.sharedkernel.metadata.IntegrationTestMetaDataInspector;
import com.daimler.sechub.sharedkernel.metadata.MapStorageMetaDataInspection;
import com.daimler.sechub.sharedkernel.metadata.MetaDataInspector;
import com.daimler.sechub.sharedkernel.validation.ProjectIdValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;
import com.daimler.sechub.storage.core.JobStorage;
import com.daimler.sechub.storage.core.StorageService;

/**
 * Contains additional rest call functionality for integration test server
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestServerRestController {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestServerRestController.class);

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private StorageService storageService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private ProjectIdValidation projectIdValidation;

    @Autowired
    private MetaDataInspector metaDataInspector;

    @Autowired
    IntegrationTestSecurityLogService securityLogService;
    
    @Autowired
    IntegrationTestEventInspectorService eventInspectorService;

    @Autowired
    LogSanitizer logSanitizer;

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/logs/security", method = RequestMethod.DELETE, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void clearSecurityLogs() {
        securityLogService.getLogData().clear();
    }
    
    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/logs/security", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public List<SecurityLogData> getSecurityLogData() {
        return securityLogService.getLogData();
    }
    
    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/event/inspection/reset-and-stop", method = RequestMethod.POST, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void resetAndStopEventInspection() {
        eventInspectorService.resetAndStop();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/event/inspection/start", method = RequestMethod.POST, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void startEventInspection() {
        eventInspectorService.start();
    }
    
    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/event/inspection/status", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public Map<String,String> statusEventInspection() {
        Map<String,String> map = new TreeMap<>();
        map.put("started", Boolean.toString(eventInspectorService.isStarted()));
        map.put("lastInspectionId", Integer.toString(eventInspectorService.getInspectionIdCounter()));
        return map;
    }
    
    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/event/inspection/history", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public String fetchEventInspectionHistory() {
        IntegrationTestEventHistory history = eventInspectorService.getHistory();
        return history.toJSON();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/alive", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void isAlive() {
        LOG.info("Integration test server check for alive called...");
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/shutdown", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void shutdownServer() {
        LOG.info("Integration test server shutdown initiated by closing context...");
        context.close();
    }

    @RolesAllowed(RoleConstants.ROLE_USER)
    @RequestMapping(path = APIConstants.API_USER + "integrationtest/check/role/user", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void checkRoleUser() {
        LOG.info("Integration test server says user '{}' has allowed role '{}' - all authorities: '{}'", userContextService.getUserId(),
                RoleConstants.ROLE_USER, userContextService.getAuthories());
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/metadata/inspections", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public List<MapStorageMetaDataInspection> fetchInspections() {
        if (!(metaDataInspector instanceof IntegrationTestMetaDataInspector)) {
            throw new IllegalStateException("Wrong meta data inspector!");
        }
        IntegrationTestMetaDataInspector itmd = (IntegrationTestMetaDataInspector) metaDataInspector;
        return itmd.getInspections();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/metadata/inspections", method = RequestMethod.DELETE, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void clearInspections() {
        if (!(metaDataInspector instanceof IntegrationTestMetaDataInspector)) {
            throw new IllegalStateException("Wrong meta data inspector!");
        }
        IntegrationTestMetaDataInspector itmd = (IntegrationTestMetaDataInspector) metaDataInspector;
        itmd.clear();
    }
    
    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/log/info", method = RequestMethod.POST, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void logInfo(@RequestBody String text) {
        LOG.info("FROM INTEGRATION-TEST:{}", text);
    }

    @RolesAllowed(RoleConstants.ROLE_OWNER)
    @RequestMapping(path = APIConstants.API_OWNER + "integrationtest/check/role/owner", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void checkRoleOwner() {
        LOG.info("Integration test server says user '{}' has allowed role '{}' - all authorities: '{}'", userContextService.getUserId(),
                RoleConstants.ROLE_OWNER, userContextService.getAuthories());
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/{projectId}/{jobUUID}/uploaded/{fileName}", method = RequestMethod.GET)
    public ResponseEntity<Resource> getUploadedFile(@PathVariable("projectId") String projectId, @PathVariable("jobUUID") UUID jobUUID,
            @PathVariable("fileName") String fileName) throws IOException {

        ValidationResult projectIdValidationResult = projectIdValidation.validate(projectId);
        if (!projectIdValidationResult.isValid()) {
            LOG.warn("Called with illegal projectId '{}'", logSanitizer.sanitize(projectId, 30));
            return ResponseEntity.notFound().build();
        }
        LOG.info("Integration test server: getJobStorage for {} {}", logSanitizer.sanitize(projectId, 30), jobUUID);
        JobStorage storage = storageService.getJobStorage(projectId, jobUUID);
        if (!storage.isExisting(fileName)) {
            throw new NotFoundException("file not uploaded:" + fileName);
        }
        InputStream inputStream = storage.fetch(fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        /* @formatter:off */
		byte[] bytes = new byte[inputStream.available()];
		new DataInputStream(inputStream).readFully(bytes);
		ByteArrayResource resource = new ByteArrayResource(bytes);

		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(resource.contentLength())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(resource);
		/* @formatter:on */

    }

}
