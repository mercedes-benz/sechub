// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

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

import com.mercedesbenz.sechub.sharedkernel.Profiles;

/**
 * When we start an integration test server new, we automatically clear former
 * persisted test data (file storage). See `PersistentScenarioTestDataProvider`
 * inside integration test project for more details.
 *
 * Reason: - builds on servers can be done without gradle clean - local
 * integration tests use similar names (can be useful for h2 server mode + SQL
 * queries)
 *
 * If this is not wanted (e.g. for testing locally with a postgres database and
 * restarting the server multiple times) a developer can launch the server with
 * system property {@value #SKIP_AUTOCLEAN_PROPERTY} having value 'true'
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class IntegrationTestServerPersistentTestDataCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestServerPersistentTestDataCleaner.class);

    private static final String SKIP_AUTOCLEAN_PROPERTY = "sechub.integrationtest.data.autoclean.skip";

    @Value("${" + SKIP_AUTOCLEAN_PROPERTY + ":false}")
    boolean autoCleanDisabled;

    @Value("${sechub.integrationtest.ignore.missing.serverproject:false}")
    boolean ignoreMissingServerProject;

    @Bean
    @Order(100)
    @Profile(Profiles.INTEGRATIONTEST)
    public CommandLineRunner dropOldIntegrationTestData() {
        LOG.info("*".repeat(100));
        LOG.info("* Integration test auto clean");
        LOG.info("* ---------------------------");
        LOG.info("* - cleans growing ids");
        LOG.info("* - cleans all local integration test data");
        LOG.info("* - can be skipped with key: {}", SKIP_AUTOCLEAN_PROPERTY);
        if (autoCleanDisabled) {
            LOG.info("* - SKIPPED");
            LOG.info("*".repeat(100));
        } else {
            LOG.info("* - STARTING");
            LOG.info("*".repeat(100));

            cleanupOldIntegrationTestData();
        }

        return args -> {
        };
    }

    private void cleanupOldIntegrationTestData() {

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
        String absolutePath = file.toPath().toAbsolutePath().toString();
        if (file.exists()) {
            LOG.info("Start removing old integration test data from {}", absolutePath);
            if (!FileSystemUtils.deleteRecursively(file)) {
                throw new IllegalStateException("Not able to destroy former integrationtest data on new integration test server startup!");
            }
        } else {
            LOG.info("No persisted integration test data found at {}", absolutePath);
        }

    }

}
