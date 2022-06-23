// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.developertools.admin.ui.ConfigurationSetup;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.pds.PDSSolutionTestFilesGenerator;

public class GeneratePDSSolutionTestFilesAction extends AbstractUIAction {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratePDSSolutionTestFilesAction.class);

    private static final long serialVersionUID = 1L;

    public GeneratePDSSolutionTestFilesAction(UIContext context) {
        super("Generate PDS solution test files", context);
    }

    @Override
    public void execute(ActionEvent e) {
        File file = getContext().getDialogUI().selectFile(
                ConfigurationSetup.PDS_SOLUTION_GENERATOR_SECHUB_CONFIGURATION_DIRECTORY.getStringValue("secub-config.json"),
                new FileNameExtensionFilter("sechub configuration files", "json"));
        if (file == null) {
            outputAsTextOnSuccess("No file selected - canceled");
            return;
        }

        Optional<String> scanTypeOpt = getContext().getDialogUI().getUserInput("Please enter scan type", ScanType.CODE_SCAN.getId());
        if (!scanTypeOpt.isPresent()) {
            outputAsTextOnSuccess("No scan type selected - canceled");
            return;
        }

        try {
            PDSSolutionTestFilesGenerator pdsSolutionGenerator = new PDSSolutionTestFilesGenerator();
            pdsSolutionGenerator.setOutputHandler(getContext().getOutputUI());
            pdsSolutionGenerator.generate(file.getAbsolutePath(), scanTypeOpt.get());

            outputAsTextOnSuccess("PDS solution test files has been created");
        } catch (Exception ex) {
            output("Was not able to generate pds solution test files:" + ex.getMessage());
            LOG.error("Was not able to import", ex);
        }
    }

}