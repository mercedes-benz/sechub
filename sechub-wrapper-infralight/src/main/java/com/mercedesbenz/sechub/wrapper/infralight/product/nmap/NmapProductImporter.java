package com.mercedesbenz.sechub.wrapper.infralight.product.nmap;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImportData;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImporter;

@Component
public class NmapProductImporter implements InfralightProductImporter {

    @Override
    public List<InfralightProductImportData> startImport(String data) {
        List<InfralightProductImportData> list = new ArrayList<>();

        return list;
    }

    @Override
    public String getName() {
        return "sslscan";
    }

    @Override
    public String getImportFileName() {
        return "ssl-scan.output";
    }

}
