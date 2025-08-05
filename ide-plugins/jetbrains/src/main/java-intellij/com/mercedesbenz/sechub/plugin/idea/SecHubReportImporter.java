// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubResult;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.SecHubFindingToFindingModelTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SecHubReportImporter {
    private static final SecHubReportImporter INSTANCE = new SecHubReportImporter();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final SecHubFindingToFindingModelTransformer transformer = SecHubFindingToFindingModelTransformer.getInstance();
    private static final SecHubReportViewUpdater secHubReportViewUpdater = SecHubReportViewUpdater.getInstance();

    public static SecHubReportImporter getInstance() {
        return INSTANCE;
    }

    public void importAndDisplayReport(File reportFile) throws IOException {
        importAndDisplayReport(reportFile, ProgressIndicatorProvider.getGlobalProgressIndicator());
    }

    public void importAndDisplayReport(File reportFile, @Nullable ProgressIndicator progressIndicator) throws IOException {
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
        if (progressIndicator != null) {
            progressIndicator.setText("Import SecHub report from " + absolutePath);
        }

        SecHubReport report;

        try {
            report = mapper.readValue(reportFile, SecHubReport.class);
        } catch (RuntimeException e) {
            throw new IOException("Unexpected error on import happened", e);
        }

        List<SecHubFinding> secHubFindings = getSecHubFindings(report);
        FindingModel model = transformer.transform(secHubFindings, Collections.emptyList());
        model.setJobUUID(report.getJobUUID());
        model.setTrafficLight(report.getTrafficLight());

        secHubReportViewUpdater.updateReportViewInAWTThread(model);
    }

    private static @NotNull List<SecHubFinding> getSecHubFindings(SecHubReport report) {
        if (report == null) {
            throw new RuntimeException("Report file importer returned null");
        }

        SecHubResult result = report.getResult();

        if (result == null) {
            throw new RuntimeException("Report file importer returned no result");
        }

        List<SecHubFinding> secHubFindings = result.getFindings();

        if (secHubFindings == null || secHubFindings.isEmpty()) {
            throw new RuntimeException("Report file importer returned no findings");
        }
        return secHubFindings;
    }

}
