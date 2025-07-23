package com.mercedesbenz.sechub.wrapper.infralight.product.nmap;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImporter;

@Component
public class NmapProductImporter implements InfralightProductImporter {

    @Override
    public List<GenericInfrascanFinding> startImport(String data) {
        
//        XMLmapper (jackson...)
        
        List<GenericInfrascanFinding> list = new ArrayList<>();

        return list;
    }

    @Override
    public String getProductName() {
        return "sslscan";
    }

    @Override
    public String getImportFileName() {
        return "nmap-output.txt";
    }

}
