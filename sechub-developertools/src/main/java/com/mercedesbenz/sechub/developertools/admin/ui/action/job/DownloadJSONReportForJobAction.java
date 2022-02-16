// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.job;

import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class DownloadJSONReportForJobAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public DownloadJSONReportForJobAction(UIContext context) {
        super("Download JSON report", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> projectId = getUserInput("Please enter project id", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }
        Optional<String> jobUUID = getUserInput("Please enter job uuid", InputCacheIdentifier.JOB_UUID);
        if (!jobUUID.isPresent()) {
            return;
        }
        UUID sechubJobUUID = null;
        try {
            sechubJobUUID = UUID.fromString(jobUUID.get());
        } catch (Exception ex) {
            getContext().getOutputUI().error("Not a UUID:" + jobUUID.get(), ex);
            return;
        }

        try {
            String data = getContext().getAdministration().fetchJSONReport(projectId.get(), sechubJobUUID);
            Path tempFile = Files.createTempFile("sechub-report-", "_" + sechubJobUUID + ".json");
            Files.write(tempFile, data.getBytes());

            outputAsTextOnSuccess("Report file downloaded to location:" + tempFile.toAbsolutePath().toString());
        } catch (Exception ex) {
            getContext().getOutputUI().error("Download of JSON report failed for job:" + jobUUID.get(), ex);
            return;
        }
    }

}