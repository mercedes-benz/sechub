// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.developertools.admin.ui.ConfigurationSetup;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.pds.tools.generator.PDSSolutionTestFilesGenerator;

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
/* @formatter:off */
        Optional<String> scanTypeOpt = getContext().getDialogUI().
                getUserInputFromCombobox("Please enter scan type",
                        "Scan type",
                        Arrays.asList(ScanType.values()).stream().
                            filter((scanType)->! (ScanType.UNKNOWN.equals(scanType) || ScanType.REPORT.equals(scanType))).
                            map((scanType) -> scanType.getId()).
                            collect(Collectors.toList()),
                        ScanType.CODE_SCAN.getId());
                /* @formatter:on */
        if (!scanTypeOpt.isPresent()) {
            outputAsTextOnSuccess("No scan type selected - canceled");
            return;
        }

        String selectedScanType = scanTypeOpt.get();
        output("Call generator with selected scan type: " + selectedScanType);

        try {
            PDSSolutionTestFilesGenerator pdsSolutionGenerator = new PDSSolutionTestFilesGenerator();
            pdsSolutionGenerator.setOutputHandler(getContext().getOutputUI());
            pdsSolutionGenerator.generate(file.getAbsolutePath(), selectedScanType, null);

            outputAsTextOnSuccess("PDS solution test files has been created");
        } catch (Exception ex) {
            output("Was not able to generate pds solution test files:" + ex.getMessage());
            LOG.error("Was not able to import", ex);
        }
    }

}