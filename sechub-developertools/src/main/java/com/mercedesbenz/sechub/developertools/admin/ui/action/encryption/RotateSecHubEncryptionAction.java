// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.encryption;

import java.awt.event.ActionEvent;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;

public class RotateSecHubEncryptionAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(RotateSecHubEncryptionAction.class);

    public RotateSecHubEncryptionAction(UIContext context) {
        super("Rotate SecHub encryption", context);
    }

    @Override
    public void execute(ActionEvent e) {

        Optional<SecHubCipherAlgorithm> optSelectedAlgorithm = getUserInputFromCombobox("Select algorithm to use for encryption",
                SecHubCipherAlgorithm.AES_GCM_SIV_256, "Select algorithm", SecHubCipherAlgorithm.values());
        if (!optSelectedAlgorithm.isPresent()) {
            return;
        }
        String sourceData = null;
        SecHubCipherPasswordSourceType sourceType;
        SecHubCipherAlgorithm selectedAlgorithm = optSelectedAlgorithm.get();
        switch (selectedAlgorithm) {
        case NONE:
            sourceType = SecHubCipherPasswordSourceType.NONE;
            break;
        case AES_GCM_SIV_128:
        case AES_GCM_SIV_256:
        default:
            sourceType = SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE;
            Optional<String> optEnvironmentVariableName = getUserInput("Please enter environment name for password");
            if (!optEnvironmentVariableName.isPresent()) {
                return;
            }
            sourceData = optEnvironmentVariableName.get();
            break;

        }
        String confirmMessage = """
                You want to start encryption rotation with:

                  Cipher algorithm : '%s'
                  Password source
                           - type: '%s'
                           - data: '%s'

                 This will update encryption pool on all SecHub servers inside cluster
                 and will also start automated re-encryption of jobs which are in state
                 CANCELED or ENDED.

                 Are you sure ?
                 """.formatted(selectedAlgorithm, sourceType, sourceData);
        if (!confirm(confirmMessage)) {
            return;
        }
        LOG.info("start encryption rotation with algorithm: {}, password souce type: {}, password source data: {}", selectedAlgorithm, sourceType, sourceData);

        SecHubEncryptionData data = new SecHubEncryptionData();
        data.setAlgorithm(selectedAlgorithm);
        data.setPasswordSourceType(sourceType);
        data.setPasswordSourceData(sourceData);

        String infoMessage = getContext().getAdministration().rotateEncryption(data);
        outputAsTextOnSuccess(infoMessage);
    }

    @Override
    protected boolean canConfirmationBeOverridenBySetup() {
        return false;
    }

}