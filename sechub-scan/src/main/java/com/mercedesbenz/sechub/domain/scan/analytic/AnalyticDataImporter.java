package com.mercedesbenz.sechub.domain.scan.analytic;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;

public interface AnalyticDataImporter {

    void importIfDataIsSupported(String dataToImportAsString, AnalyticData target);

}
