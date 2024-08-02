// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.encryption;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.encryption.DefaultSecretKeyProvider;
import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;

public class TestDecryptToStringAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    private static final PersistentCipherFactory cipherFactory = new PersistentCipherFactory();
    private static final EncryptionSupport encryptionSupport = new EncryptionSupport();

    public TestDecryptToStringAction(UIContext context) {
        super("Test decrypt to string", context);
    }

    @Override
    public void execute(ActionEvent e) {

        /* -------algorithm--------------------------- */
        Optional<SecHubCipherAlgorithm> optSelectedAlgorithm = getUserInputFromCombobox("Select algorith to use for encryption",
                SecHubCipherAlgorithm.AES_GCM_SIV_256, "Select algorithm", SecHubCipherAlgorithm.values());
        if (!optSelectedAlgorithm.isPresent()) {
            return;
        }
        SecHubCipherAlgorithm selectedAlgorithm = optSelectedAlgorithm.get();

        /* ------secret key---------------------------- */
        Optional<String> optBase64testSecretKey = getUserPassword("Base64 test secret key", null);
        if (optBase64testSecretKey.isEmpty()) {
            return;
        }
        String base64 = optBase64testSecretKey.get();
        byte[] decoded = Base64.getDecoder().decode(base64.getBytes());
        SecretKeyProvider secretKeyProvider = null;

        switch (selectedAlgorithm) {
        case NONE:
            secretKeyProvider = null;
            break;
        default:
            secretKeyProvider = new DefaultSecretKeyProvider(decoded, selectedAlgorithm.getType());
        }

        output("secret key provider:" + secretKeyProvider);

        /* ------create and use cipher---------------------- */
        PersistentCipher cipher = cipherFactory.createCipher(secretKeyProvider, selectedAlgorithm.getType());

        /* ------select files------------------------------- */
        File encryptedDataFile = getContext().getDialogUI().selectFile(System.getenv("HOME"), "Please select encrypted data file");
        if (encryptedDataFile == null) {
            return;
        }
        File initialVectorFile = getContext().getDialogUI().selectFile(System.getenv("HOME"), "Please select initial vector file");
        if (initialVectorFile == null) {
            return;
        }

        /* ------decrypt------------------------------- */
        try {
            byte[] dataBytes = Files.readAllBytes(encryptedDataFile.toPath());
            byte[] vectorBytes = Files.readAllBytes(initialVectorFile.toPath());

            String decryptedAsString = encryptionSupport.decryptString(dataBytes, cipher, new InitializationVector(vectorBytes));
            File file = new File(encryptedDataFile.getParentFile(), encryptedDataFile.getName() + ".txt");
            Files.writeString(file.toPath(), decryptedAsString);

            output("Decrypted file written to " + file);

        } catch (Exception ex) {
            error(ex.getMessage());
            ex.printStackTrace();
        }

    }

}
