// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.analytic;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticDataPart;
import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;

@Service
public class AnalyticDataImportService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticDataImportService.class);

    @Autowired
    List<AnalyticDataPartImporter<?>> importers;

    /**
     * Imports given analytic data string into given target
     *
     * @param analyticDataAsString
     * @param target
     */
    public void importAnalyticDataParts(String analyticDataAsString, AnalyticData target) {
        int amountOfImports = 0;

        for (AnalyticDataPartImporter<?> importer : importers) {
            try {
                if (!importer.isAbleToImport(analyticDataAsString)) {
                    continue;
                }

                AnalyticDataPart importedDataPart = importer.importData(analyticDataAsString);
                if (importedDataPart instanceof CodeAnalyticData) {
                    CodeAnalyticData importedCodeAnalyticData = (CodeAnalyticData) importedDataPart;

                    /* check if already existing code analytic data exists */
                    if (target.getCodeAnalyticData().isPresent()) {

                        CodeAnalyticData existingCodeAnalyticData = target.getCodeAnalyticData().get();
                        LOG.warn("There was already code analytic data from: {}. This will now be overriden by results from: {} ",
                                existingCodeAnalyticData.getProductInfo(), importedCodeAnalyticData.getProductInfo());
                    }

                    target.setCodeAnalyticData(importedCodeAnalyticData);
                    LOG.debug("Imported code analytic data with: {}. Found languages: {}", importer.getClass().getSimpleName(),
                            importedCodeAnalyticData.getLanguages());

                    amountOfImports++;
                }

            } catch (IOException e) {
                LOG.error("Was not able to import with importer:{}", importer.getClass(), e);
            }
        }
        if (amountOfImports == 0) {
            LOG.warn("Given analytic data was not imported by any importer!");
        } else if (amountOfImports > 1) {
            LOG.warn("Same analytic data was imported by {} importers!", amountOfImports);
        }

    }

}
