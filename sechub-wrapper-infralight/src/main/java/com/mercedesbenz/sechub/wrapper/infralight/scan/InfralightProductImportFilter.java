package com.mercedesbenz.sechub.wrapper.infralight.scan;

import java.util.List;

public interface InfralightProductImportFilter {
    
   public boolean canFilter(InfralightProductImporter importer);

   public void filter(List<InfraScanProductImportData> importData);
   

}
