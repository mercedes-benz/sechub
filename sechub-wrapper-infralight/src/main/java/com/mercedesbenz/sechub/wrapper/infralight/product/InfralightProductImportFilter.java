package com.mercedesbenz.sechub.wrapper.infralight.product;

import java.util.List;

public interface InfralightProductImportFilter {

    public boolean canFilter(InfralightProductImporter importer);

    public void filter(List<InfralightProductImportData> importData);

}
