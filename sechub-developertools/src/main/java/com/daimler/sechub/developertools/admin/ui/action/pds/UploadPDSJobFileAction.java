// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import javax.swing.JFileChooser;

import com.daimler.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class UploadPDSJobFileAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;
    private static JFileChooser  fileChooser = new JFileChooser();

    public UploadPDSJobFileAction(UIContext context) {
        super("Upload a file to PDS job", context);
    }

    @Override
    protected void executePDS(PDSAdministration pds) {
        Optional<String> pdsJobUUID = getUserInput("PSD job uuid", InputCacheIdentifier.PDS_JOBUUID);
        if (!pdsJobUUID.isPresent()) {
            output("cancel pds job uuid");
            return;
        }
        Optional<String> fileNameOpt = getUserInput("PSD job uuid", "sourcecode.zip");
        if (!fileNameOpt.isPresent()) {
            output("cancel pds job uuid");
            return;
        }
        
        int dialogResult = fileChooser.showOpenDialog(getContext().getFrame());
        if (dialogResult != JFileChooser.APPROVE_OPTION) {
            output("cancel file selection for upload");
            return;
        }
        File file = fileChooser.getSelectedFile();
        String result = pds.upload(UUID.fromString(pdsJobUUID.get()),file,fileNameOpt.get());
        
        outputAsTextOnSuccess(result);

    }

}