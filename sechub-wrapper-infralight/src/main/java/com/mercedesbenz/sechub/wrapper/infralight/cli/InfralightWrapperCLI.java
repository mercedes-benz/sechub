// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.infralight.cli;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.wrapper.infralight.scan.InfralightSarifImportService;

import de.jcup.sarif_2_1_0.model.SarifSchema210;

@Component
public class InfralightWrapperCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InfralightWrapperCLI.class);

    @Autowired
    InfralightSarifImportService scanService;

    @Autowired
    InfralightWrapperEnvironment environment;

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Infralight wrapper starting");

        String pathAsString = environment.getInfrascanProductsOutputFolder();
        LOG.info("Import from product output folder: {}", pathAsString);
        Path productsOutputFolder = Paths.get(pathAsString);

        SarifSchema210 sarifResult = scanService.importProductResultsAsSarif(productsOutputFolder);
        String sarifAsJson = JSONConverter.get().toJSON(sarifResult);  
        
        String pdsResultFilePath = environment.getPdsResultFile();
        
        TextFileWriter writer = new TextFileWriter();
        
        writer.writeTextToFile(new File(pdsResultFilePath), sarifAsJson, true);

    }

}
