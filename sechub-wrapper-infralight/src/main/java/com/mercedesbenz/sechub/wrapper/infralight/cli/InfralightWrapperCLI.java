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
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralighProductImportService;

import de.jcup.sarif_2_1_0.model.SarifSchema210;

@Component
public class InfralightWrapperCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InfralightWrapperCLI.class);

    @Autowired
    InfralighProductImportService scanService;

    @Autowired
    InfralightWrapperEnvironment environment;

    @Autowired
    TextFileWriter textFileWriter;

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Infralight wrapper starting");

        String productsOutputFolderAsString = environment.getInfrascanProductsOutputFolder();
        if (productsOutputFolderAsString == null || productsOutputFolderAsString.isBlank()) {
            throw new IllegalArgumentException("Path to infrascan products output folder may not be null or empty");
        }
        String pdsResultFilePath = environment.getPdsResultFile();
        if (pdsResultFilePath == null || pdsResultFilePath.isBlank()) {
            throw new IllegalArgumentException("Path to PDS rsult file may not be null or empty");
        }

        Path productsOutputFolder = Paths.get(productsOutputFolderAsString);

        SarifSchema210 sarifResult = scanService.importProductResultsAsSarif(productsOutputFolder);
        String sarifAsJson = JSONConverter.get().toJSON(sarifResult);

        textFileWriter.writeTextToFile(new File(pdsResultFilePath), sarifAsJson, true);

    }

}
