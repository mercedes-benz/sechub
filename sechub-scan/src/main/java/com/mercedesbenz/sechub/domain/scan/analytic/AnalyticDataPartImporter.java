package com.mercedesbenz.sechub.domain.scan.analytic;

import java.io.IOException;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticDataPart;

public interface AnalyticDataPartImporter<T extends AnalyticDataPart> {

    T importData(String dataToImportAsString) throws IOException;

    boolean isAbleToImport(String analyticDataAsString);

}
