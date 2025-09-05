package com.mercedesbenz.sechub.wrapper.infralight.product.nmap;

import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImportFilter;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImporter;

@Component
public class NmapPortScanProductImportFilter implements InfralightProductImportFilter {

    @Override
    public boolean canFilter(InfralightProductImporter importer) {
        return importer instanceof NmapPortScanProductImporter;
    }

    @Override
    public void filter(List<GenericInfrascanFinding> importData) {

    }

}
