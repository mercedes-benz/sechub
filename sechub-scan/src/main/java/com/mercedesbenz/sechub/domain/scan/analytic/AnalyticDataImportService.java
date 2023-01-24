package com.mercedesbenz.sechub.domain.scan.analytic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;


@Service
public class AnalyticDataImportService {

    @Autowired
    List<AnalyticDataImporter> importers;

    public void importAnalyticDataIntoModel(String analyticDataAsString, AnalyticData target) {
        for (AnalyticDataImporter importer : importers) {
            importer.importIfDataIsSupported(analyticDataAsString, target);
        }

    }

}
