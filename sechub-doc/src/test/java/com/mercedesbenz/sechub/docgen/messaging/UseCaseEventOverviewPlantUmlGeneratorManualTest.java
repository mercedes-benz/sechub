// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.messaging;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.test.TestConstants;

class UseCaseEventOverviewPlantUmlGeneratorManualTest {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseEventOverviewPlantUmlGeneratorManualTest.class);

    @Test
    @EnabledIfSystemProperty(named = TestConstants.MANUAL_TEST_BY_DEVELOPER, matches = "true", disabledReason = TestConstants.DESCRIPTION_DISABLED_BECAUSE_A_MANUAL_TEST_FOR_GENERATION)
    void manualTestByDeveloper() {
        System.setProperty("com.mercedesbenz.sechub.docgen.debug", "true");

        File sourceFolder = new File("./../sechub-integrationtest/build/test-results/event-trace");
        File outputFolder = new File("./build/tmp/puml/");

        LOG.info("start plantuml generation with \nsource folder:{}\noutputfolder :{}", sourceFolder.getAbsolutePath(), outputFolder.getAbsolutePath());

        UseCaseEventOverviewPlantUmlGenerator generator = new UseCaseEventOverviewPlantUmlGenerator(sourceFolder, outputFolder);
        generator.generateAndRememberUsecaseNamesToMessageIdMapping();
    }
}
