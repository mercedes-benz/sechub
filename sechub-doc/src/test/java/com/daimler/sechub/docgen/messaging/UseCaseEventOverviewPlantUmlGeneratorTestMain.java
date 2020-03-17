package com.daimler.sechub.docgen.messaging;

import java.io.File;

public class UseCaseEventOverviewPlantUmlGeneratorTestMain {
    public static void main(String[] args) {
        File sourceFolder = new File("./../sechub-integrationtest/build/test-results/event-trace");
        File outputFolder = new File("./build/temp/puml/");
        
        UseCaseEventOverviewPlantUmlGenerator generator = new UseCaseEventOverviewPlantUmlGenerator(sourceFolder,outputFolder);
        generator.generate();
    }
}
