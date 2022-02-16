// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.messaging;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UseCaseEventOverviewPlantUmlGeneratorTestMain {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseEventOverviewPlantUmlGeneratorTestMain.class);

    public static void main(String[] args) {
        System.setProperty("com.daimler.sechub.docgen.debug", "true");

        File sourceFolder = new File("./../sechub-integrationtest/build/test-results/event-trace");
        File outputFolder = new File("./build/tmp/puml/");

        LOG.info("start plantuml generation with \nsource folder:{}\noutputfolder :{}", sourceFolder.getAbsolutePath(), outputFolder.getAbsolutePath());

        UseCaseEventOverviewPlantUmlGenerator generator = new UseCaseEventOverviewPlantUmlGenerator(sourceFolder, outputFolder);
        generator.generate();
    }
}
