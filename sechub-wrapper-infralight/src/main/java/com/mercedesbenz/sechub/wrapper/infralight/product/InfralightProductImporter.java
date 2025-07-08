package com.mercedesbenz.sechub.wrapper.infralight.product;

import java.util.List;

public interface InfralightProductImporter {

    List<InfralightProductImportData> startImport(String data);

    /**
     * Returns then name of the product to import
     *
     * @return product name
     */
    public String getName();

    /**
     * Returns the name of the output file of the product
     *
     * @return file name
     */
    public String getImportFileName();

}
