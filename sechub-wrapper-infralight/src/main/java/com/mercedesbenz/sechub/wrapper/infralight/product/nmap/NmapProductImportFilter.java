package com.mercedesbenz.sechub.wrapper.infralight.product.nmap;

import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImportData;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImportFilter;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImporter;

@Component
public class NmapProductImportFilter implements InfralightProductImportFilter {

    @Override
    public boolean canFilter(InfralightProductImporter importer) {
        return importer instanceof NmapProductImporter;
    }

    @Override
    public void filter(List<InfralightProductImportData> importData) {

    }

}
