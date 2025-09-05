package com.mercedesbenz.sechub.wrapper.infralight.product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileReader;

@Component
public class InfralightProductImportStringDataProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(InfralightProductImportStringDataProvider.class);
    
    @Autowired
    private TextFileReader textFileReader;

    /**
     * Returns string data for importer - if data is found for product
     *
     * @return data or <code>null</code>
     */
    public String getStringDataForImporter(InfralightProductImporter importer, Path productsOutputFolder) throws IOException {

        String filename = importer.getImportFileName();
        Path target = productsOutputFolder.resolve(filename);
        logger.info("Try to import data for product '{}' at '{}'", importer.getProductName(), target);
        if (!Files.exists(target)) {
            logger.info("No data to import found for product '{}'", importer.getProductName());
            return null;
        }
        String data = textFileReader.readTextFromFile(target.toFile());
        logger.info("Data to import found for product '{}'", importer.getProductName());
        return data;
    }

}
