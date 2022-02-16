// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import com.daimler.sechub.sharedkernel.Profiles;

/**
 * When we start an integration test server new, we automatically clear former
 * persisted test data (file storage). See `PersistentScenarioTestDataProvider`
 * inside integration test project for more details.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class IntegrationTestServerPersistentTestDataCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestServerPersistentTestDataCleaner.class);

    @Value("${sechub.integrationtest.ignore.missing.serverproject:false}")
    boolean ignoreMissingServerProject;

    @Bean
    @Order(100)
    @Profile(Profiles.INTEGRATIONTEST)
    public CommandLineRunner dropIntegrationTestData() {
        /*
         * When we start a new integration test server, we always drop former persisted
         * integration test data
         */
        File parent = new File("./sechub-integrationtest");
        if (!parent.exists()) {
            parent = new File("./../sechub-integrationtest");
            if (!parent.exists()) {
                if (ignoreMissingServerProject) {
                    LOG.warn(
                            "Server project cannot be found, but is ignored so integration test server starts. But be aware that your testdata will not be cleaned on filesystem, if you run junit tests");
                } else {
                    throw new IllegalStateException("cannot find sechub-server project");
                }
            }
        }
        File file = new File(parent, "build/sechub/integrationtest");
        if (file.exists()) {
            LOG.info("Start removing old integration test data from {}", file.getAbsolutePath());
            if (!FileSystemUtils.deleteRecursively(file)) {
                throw new IllegalStateException("Not able to destroy former integrationtest data on new integration test server startup!");
            }
        } else {
            LOG.info("No persisted integration test data found at {}", file.getAbsolutePath());
        }
        return args -> {
        };
    }

}
