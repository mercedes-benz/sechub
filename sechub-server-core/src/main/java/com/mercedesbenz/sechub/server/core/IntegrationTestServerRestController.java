// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.IntegrationTestAutoCleanupResultInspector;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.IntegrationTestAutoCleanupResultInspector.JsonDeleteCount;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.IntegrationTestSecurityLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogData;
import com.mercedesbenz.sechub.sharedkernel.messaging.IntegrationTestEventHistory;
import com.mercedesbenz.sechub.sharedkernel.messaging.IntegrationTestEventInspectorService;
import com.mercedesbenz.sechub.sharedkernel.metadata.IntegrationTestMetaDataInspector;
import com.mercedesbenz.sechub.sharedkernel.metadata.MapStorageMetaDataInspection;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspector;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.security.AuthorityConstants;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.storage.SecHubStorageService;
import com.mercedesbenz.sechub.sharedkernel.validation.ProjectIdValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;
import com.mercedesbenz.sechub.storage.core.JobStorage;

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
    private SecHubStorageService storageService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private ProjectIdValidation projectIdValidation;

    @Autowired
    private MetaDataInspector metaDataInspector;

    @Autowired
    private IntegrationTestAutoCleanupResultInspector autoCleanupResultInspector;

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

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/autocleanup/inspection/reset", method = RequestMethod.POST, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void resetAutoCleanupInspection() {
        autoCleanupResultInspector.reset();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/autocleanup/inspection/deleteCounts", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public List<JsonDeleteCount> fetchAutoCleanupInspectionDeleteCounts() {
        return autoCleanupResultInspector.createList();
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
    public Map<String, String> statusEventInspection() {
        Map<String, String> map = new TreeMap<>();
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

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/check/role/{role}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public boolean checkRole(@PathVariable("role") String role) {
        String authories = userContextService.getAuthorities();
        String userId = userContextService.getUserId();

        LOG.info("Integration test server wants to know if current user '{}' has role '{}'", userId, role);

        boolean hasRole = false;
        if (authories != null) {
            String authority = AuthorityConstants.AUTHORITY_ROLE_PREFIX + role.toUpperCase();
            hasRole = authories.indexOf(authority) != -1;

            LOG.debug("Check if authRole '{}' contained in authorities '{}'", authority, authories);
        } else {
            LOG.info("No authorities found - return false");
        }

        LOG.info("Result: User '{}' has role {}={}", userId, role, hasRole);
        return hasRole;
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

        JobStorage storage = storageService.createJobStorageForProject(projectId, jobUUID);
        if (!storage.isExisting(fileName)) {
            throw new NotFoundException("file not uploaded:" + fileName);
        }
        InputStream inputStream = storage.fetch(fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        /* @formatter:off */
		InputStreamResource resource = new InputStreamResource(inputStream);
		ResponseEntity<Resource> result = ResponseEntity.ok()
				.headers(headers)
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(resource);

		storage.close();

		return result;
		/* @formatter:on */

    }

}
