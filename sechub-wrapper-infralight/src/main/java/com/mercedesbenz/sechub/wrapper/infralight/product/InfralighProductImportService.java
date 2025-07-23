package com.mercedesbenz.sechub.wrapper.infralight.product;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanProductData;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanResult;

@Service
public class InfralighProductImportService {

    private static final Logger LOG = LoggerFactory.getLogger(InfralighProductImportService.class);

    @Autowired
    List<InfralightProductImporter> productImporters = new ArrayList<>();

    @Autowired
    List<InfralightProductImportFilter> productImportFilters = new ArrayList<>();

    @Autowired
    InfralightProductImportStringDataProvider dataProvider;

    public GenericInfrascanResult importGenericInfrascanResult(Path productsOutputFolder) throws IOException {
        LOG.info("Import from product output folder: {}", productsOutputFolder);
        GenericInfrascanResult result = new GenericInfrascanResult();

        for (InfralightProductImporter importer : productImporters) {

            String data = dataProvider.getStringDataForImporter(importer, productsOutputFolder);
            if (data == null) {
                /* means no data found - skip import here */
                LOG.debug("Skip import for '{}' because no data available", importer.getProductName());
                continue;
            }

            List<GenericInfrascanFinding> importData = importer.startImport(data);

            for (InfralightProductImportFilter filter : productImportFilters) {
                if (filter.canFilter(importer)) {
                    filter.filter(importData);
                }
            }

            GenericInfrascanProductData productData = new GenericInfrascanProductData();
            productData.setProduct(importer.getProductName());
            productData.getFindings().addAll(importData);
            
            result.getProducts().add(productData);
            
        }

        return result;
    }


}
