// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.pds.job.PDSJob;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;
import com.mercedesbenz.sechub.pds.storage.IntegrationTestPDSStorageInfoCollector;

/**
 * Contains additional rest call functionality for integration test server
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@Profile(PDSProfiles.INTEGRATIONTEST)
public class IntegrationTestPDSRestController {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestPDSRestController.class);

    @Autowired
    IntegrationTestPDSStorageInfoCollector storageInfoCollector;

    @Autowired
    PDSJobRepository jobRepository;

    @Autowired
    private ConfigurableApplicationContext context;

    @RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "integrationtest/alive", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void isAlive() {
        LOG.info("Integration test pds check for alive called...");
    }

    @RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "integrationtest/shutdown", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void shutdownServer() {
        LOG.info("Integration test server shutdown initiated by closing context...");
        context.close();
    }

    @RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "integrationtest/log/info", method = RequestMethod.POST, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void logInfo(@RequestBody String text) {
        LOG.info("FROM INTEGRATION-TEST:{}", text);
    }

    @RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "integrationtest/storage/{jobUUID}/path", method = RequestMethod.GET, produces = {
            MediaType.TEXT_PLAIN_VALUE })
    public String fetchStoragePathForJobUUID(@PathVariable("jobUUID") UUID jobUUID) {
        String storagePathFound = storageInfoCollector.getFetchedJobUUIDStoragePathHistory().get(jobUUID);

        LOG.info("Integration test checks storage path for job uuid:{} - result:{}", jobUUID, storagePathFound);
        return storagePathFound;
    }

    @RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "integrationtest/last/started/job/uuid", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public String fetchLastStartedPDSJobUUID() {
        List<PDSJob> jobs = jobRepository.findAll(Sort.by(Direction.DESC, PDSJob.PROPERTY_STARTED));
        Iterator<PDSJob> it = jobs.iterator();
        if (it.hasNext()) {
            PDSJob job = it.next();
            return job.getUUID().toString();
        }
        return null;
    }

}
