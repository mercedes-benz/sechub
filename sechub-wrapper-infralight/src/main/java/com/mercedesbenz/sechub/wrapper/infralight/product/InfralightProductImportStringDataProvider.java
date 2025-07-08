package com.mercedesbenz.sechub.wrapper.infralight.product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileReader;

@Component
public class InfralightProductImportStringDataProvider {

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
        if (!Files.exists(target)) {
            return null;
        }
        String data = textFileReader.readTextFromFile(target.toFile());
        return data;
    }

}
