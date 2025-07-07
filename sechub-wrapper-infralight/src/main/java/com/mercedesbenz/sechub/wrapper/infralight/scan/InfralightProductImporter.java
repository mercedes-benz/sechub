package com.mercedesbenz.sechub.wrapper.infralight.scan;

import java.util.List;

public interface InfralightProductImporter {

    List<InfraScanProductImportData> startImport(String data);
    
    public String getProductName();
    
    public default String getProductOutputFileName() {
        String productName= getProductName();
        if (productName==null || productName.isBlank()){
            throw new IllegalStateException("Product name not set - cannot determine output file name!");
        }
        return productName.toLowerCase()+".output";
    }

}
