package com.mercedesbenz.sechub.wrapper.infralight.product.sslscan;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImporter;

@Component
public class SSLScanProductImporter implements InfralightProductImporter {

    @Override
    public List<GenericInfrascanFinding> startImport(String data) {
        List<GenericInfrascanFinding> list = new ArrayList<>();

        return list;
    }

    @Override
    public String getProductName() {
        return "sslscan";
    }

    @Override
    public String getImportFileName() {
        return "sslscan-output.xml";
    }

}
