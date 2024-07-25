// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sereco.importer.ProductResultImporter;

import jakarta.annotation.PostConstruct;

@Component
public class ImporterRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ImporterRegistry.class);

    @Autowired
    List<ProductResultImporter> importers;

    @PostConstruct
    void postConstruct() {
        List<String> importerNames = new ArrayList<>();
        for (ProductResultImporter importer : importers) {
            importerNames.add(importer.getName());
        }
        LOG.info("Registered SERECO importers are: {}", importerNames);
    }

    public List<ProductResultImporter> getImporters() {
        return importers;
    }

}
