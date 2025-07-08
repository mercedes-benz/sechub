package com.mercedesbenz.sechub.wrapper.infralight.product;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.jcup.sarif_2_1_0.model.SarifSchema210;

@Service
public class InfralighProductImportService {

    private static final Logger LOG = LoggerFactory.getLogger(InfralighProductImportService.class);

    @Autowired
    List<InfralightProductImporter> productImporters = new ArrayList<>();

    @Autowired
    List<InfralightProductImportFilter> productImportFilters = new ArrayList<>();

    @Autowired
    InfralightProductImportStringDataProvider dataProvider;

    public SarifSchema210 importProductResultsAsSarif(Path productsOutputFolder) throws IOException {
        LOG.info("Import from product output folder: {}", productsOutputFolder);
        SarifSchema210 schema = new SarifSchema210();

        for (InfralightProductImporter importer : productImporters) {

            String data = dataProvider.getStringDataForImporter(importer, productsOutputFolder);
            if (data == null) {
                continue;
            }

            List<InfralightProductImportData> importData = importer.startImport(data);

            for (InfralightProductImportFilter filter : productImportFilters) {
                if (filter.canFilter(importer)) {
                    filter.filter(importData);
                }
            }

            importIntoSarifSchema(importData, schema);
        }

        return schema;
    }

    private void importIntoSarifSchema(List<InfralightProductImportData> importData, SarifSchema210 schema) {
        // FIXME de-jcup : implement

    }

}
