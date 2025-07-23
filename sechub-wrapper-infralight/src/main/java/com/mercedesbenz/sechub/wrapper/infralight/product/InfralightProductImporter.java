package com.mercedesbenz.sechub.wrapper.infralight.product;

import java.util.List;

import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;

public interface InfralightProductImporter {

    List<GenericInfrascanFinding> startImport(String data);

    /**
     * Returns then name of the product to import
     *
     * @return product name
     */
    public String getProductName();

    /**
     * Returns the name of the output file of the product
     *
     * @return file name
     */
    public String getImportFileName();

}
