package com.mercedesbenz.sechub.wrapper.infralight.product;

import java.util.List;

import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;

public interface InfralightProductImportFilter {

    public boolean canFilter(InfralightProductImporter importer);

    public void filter(List<GenericInfrascanFinding> importData);

}
