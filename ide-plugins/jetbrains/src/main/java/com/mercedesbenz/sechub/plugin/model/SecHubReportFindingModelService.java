package com.mercedesbenz.sechub.plugin.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.mercedesbenz.sechub.api.internal.gen.model.*;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccess;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccessFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SecHubReportFindingModelService {

    private static final Logger LOG = Logger.getInstance(SecHubReportFindingModelService.class);
    private static final SecHubReportFindingModelService instance = new SecHubReportFindingModelService();
    private static final SecHubFindingToFindingModelTransformer secHubReportTransformer = SecHubFindingToFindingModelTransformer.getInstance();

    private SecHubReportFindingModelService() {
        /* private constructor to enforce singleton */
    }

    public static SecHubReportFindingModelService getInstance() {
        return instance;
    }

    public FindingModel fetchAndBuildFindingModel(String projectId, UUID jobUUID, @Nullable ProgressIndicator progressIndicator) {
        if (progressIndicator != null) {
            progressIndicator.setText("Fetch SecHub report from project" + projectId + "with job UUID: " + jobUUID);
        }

        SecHubAccess secHubAccess = SecHubAccessFactory.create();

        SecHubReport report = secHubAccess.getSecHubReport(projectId, jobUUID);

        if (report == null) {
            String errMsg = "Failed to fetch SecHub report for job UUID: " + jobUUID;
            LOG.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        SecHubResult result = report.getResult();

        if (result == null) {
            String errMsg = "SecHub report result is null for job UUID: " + jobUUID;
            LOG.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        List<SecHubFinding> findings = result.getFindings();

        if (findings == null) {
            LOG.debug("SecHub report findings is empty");
            return new FindingModel();
        }

        FalsePositiveProjectConfiguration falsePositiveProjectConfiguration = secHubAccess.getFalsePositveProjectConfiguration(projectId);

        if (falsePositiveProjectConfiguration == null) {
            LOG.debug("SecHub false positive configuration is null for project: " + projectId);
            falsePositiveProjectConfiguration = new FalsePositiveProjectConfiguration();
        }

        List<FalsePositiveEntry> falsePositiveEntries = falsePositiveProjectConfiguration.getFalsePositives();

        FindingModel model = secHubReportTransformer.transform(findings, falsePositiveEntries);
        model.setProjectId(projectId);
        model.setJobUUID(report.getJobUUID());
        model.setTrafficLight(report.getTrafficLight());

        return model;
    }
}
