package com.mercedesbenz.sechub.domain.scan.analytic;

import java.io.IOException;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;

public interface AnalyticDataImporter {

    AnalyticData importData(String dataToImportAsString) throws IOException;

}
