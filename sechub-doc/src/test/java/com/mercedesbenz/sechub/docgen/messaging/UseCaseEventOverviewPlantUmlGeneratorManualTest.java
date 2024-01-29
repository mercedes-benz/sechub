// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.messaging;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.test.ManualTest;

class UseCaseEventOverviewPlantUmlGeneratorManualTest implements ManualTest {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseEventOverviewPlantUmlGeneratorManualTest.class);

    @Test
    void manualTestByDeveloper() {
        System.setProperty("com.mercedesbenz.sechub.docgen.debug", "true");

        File sourceFolder = new File("./../sechub-integrationtest/build/test-results/event-trace");
        File outputFolder = new File("./build/tmp/puml/");

        LOG.info("start plantuml generation with \nsource folder:{}\noutputfolder :{}", sourceFolder.getAbsolutePath(), outputFolder.getAbsolutePath());

        UseCaseEventOverviewPlantUmlGenerator generator = new UseCaseEventOverviewPlantUmlGenerator(sourceFolder, outputFolder);
        generator.generateAndRememberUsecaseNamesToMessageIdMapping();
    }
}
