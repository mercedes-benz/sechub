package com.mercedesbenz.sechub.domain.scan.analytic;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;

@Service
public class AnalyticDataImportService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticDataImportService.class);

    @Autowired
    List<AnalyticDataImporter> importers;

    public void importAnalyticDataIntoModel(String analyticDataAsString, AnalyticData target) {
        for (AnalyticDataImporter importer : importers) {
            try {
                AnalyticData imported = importer.importData(analyticDataAsString);
                merge(imported, target);
            } catch (IOException e) {
                LOG.error("Was not able timport analytic data with importer:{}", importer.getClass(), e);
            }
        }

    }

    private void merge(AnalyticData imported, AnalyticData target) {
        /* FIXME Albert Tregnaghi, 2023-01-26:implement... */
    }

}
