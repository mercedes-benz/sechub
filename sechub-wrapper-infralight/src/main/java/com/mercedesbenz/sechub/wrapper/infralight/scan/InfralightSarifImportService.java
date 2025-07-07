package com.mercedesbenz.sechub.wrapper.infralight.scan;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.infralight.cli.InfralightWrapperEnvironment;

import de.jcup.sarif_2_1_0.model.SarifSchema210;

@Service
public class InfralightSarifImportService {
    
    @Autowired
    List<InfralightProductImporter> productImporters = new ArrayList<>();
    
    @Autowired
    List<InfralightProductImportFilter> productImportFilters = new ArrayList<>();
    
    @Autowired
    InfralightProductImportFileReader reader;

    public SarifSchema210 importProductResultsAsSarif(Path productsOutputFolder) {
        
        SarifSchema210 schema = new SarifSchema210();
        
     
        
        for (InfralightProductImporter importer: productImporters) {
            
            String filename = importer.getProductOutputFileName();
            String data = reader.read(productsOutputFolder, filename);
            
            List<InfraScanProductImportData> importData= importer.startImport(data);
            
            for (InfralightProductImportFilter filter: productImportFilters) {
                if (filter.canFilter(importer)) {
                    filter.filter(importData);
                }
            }
            
        }
        
        return schema;
    }

}
