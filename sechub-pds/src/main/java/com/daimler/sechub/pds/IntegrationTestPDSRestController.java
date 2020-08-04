// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    private ConfigurableApplicationContext context;
    
    @RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "integrationtest/alive", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void isAlive() {
        LOG.info("Integration test pds check for alive called...");
    }

    @RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "integrationtest/shutdown", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void shutdownServer() {
        LOG.info("Integration test server shutdown initiated by closing context...");
        context.close();
    }

    
    @RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "integrationtest/log/info", method = RequestMethod.POST, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void logInfo(@RequestBody String text) {
        LOG.info("FROM INTEGRATION-TEST:{}", text);
    }

}
