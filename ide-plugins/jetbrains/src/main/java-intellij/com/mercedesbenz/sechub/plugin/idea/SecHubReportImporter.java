// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.mercedesbenz.sechub.api.SecHubReport;
import com.mercedesbenz.sechub.api.SecHubReportException;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.SecHubFindingToFindingModelTransformer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SecHubReportImporter {
    private static final SecHubReportImporter INSTANCE = new SecHubReportImporter();

    private SecHubFindingToFindingModelTransformer transformer;
    private SecHubReportViewUpdater secHubReportViewUpdater;


    public static SecHubReportImporter getInstance() {
        return INSTANCE;
    }

    public SecHubReportImporter(){
        transformer = new SecHubFindingToFindingModelTransformer();
        secHubReportViewUpdater = new SecHubReportViewUpdater();
    }

    public void importAndDisplayReport(File reportFile) throws IOException {
        importAndDisplayReport(reportFile, ProgressIndicatorProvider.getGlobalProgressIndicator());
    }

    public void importAndDisplayReport(File reportFile, ProgressIndicator progressIndicator) throws IOException {
        if (reportFile == null) {
            throw new IOException("No report file defined");
        }
        if (!reportFile.isFile()) {
            throw new IOException("Is not a file:" + reportFile);
        }
        if (!reportFile.canRead()) {
            throw new IOException("No permissions to read the report:" + reportFile);
        }
        String absolutePath = reportFile.getAbsolutePath();
        if (progressIndicator!=null){
            progressIndicator.setText("Import SecHub report from " + absolutePath);
        }

        try {
            SecHubReport report = SecHubReport.fromFile(reportFile);
            if (report == null) {
                throw new IOException("Report file importer returned null");
            }

            List<SecHubFinding> secHubFindings = report.getResult().getFindings();

            FindingModel model = transformer.transform(secHubFindings);
            model.setJobUUID(report.getJobUUID());
            model.setTrafficLight(report.getTrafficLight());

            secHubReportViewUpdater.updateReportViewInAWTThread(report.getJobUUID(), report.getTrafficLight(), model);

        } catch (SecHubReportException e) {
            throw new IOException("An error occured while reading the report: " + absolutePath
                    + ". Make sure the report is an actual SecHub Report.", e);
        } catch (RuntimeException e) {
            throw new IOException("Unexpected error on import happened", e);
        }
    }


}
