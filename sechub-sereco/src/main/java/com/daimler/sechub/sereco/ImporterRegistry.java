// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sereco.importer.ProductResultImporter;

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
        LOG.info("Registered SERECO importers are:{}", importerNames);
    }

    public List<ProductResultImporter> getImporters() {
        return importers;
    }

}
