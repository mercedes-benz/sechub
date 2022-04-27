// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import javax.swing.JFileChooser;

import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class UploadPDSJobFileAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;
    private static JFileChooser fileChooser = new JFileChooser();

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
        Optional<String> fileNameOpt = getUserInput("Filename to use on server side (be aware - we got fixed names here at the moment!))",
                CommonConstants.FILENAME_SOURCECODE_ZIP);
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
        String result = pds.upload(UUID.fromString(pdsJobUUID.get()), file, fileNameOpt.get());

        outputAsTextOnSuccess(result);

    }

}