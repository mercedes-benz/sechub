package com.mercedesbenz.sechub.domain.schedule.encryption;

import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;

public class ScheduleEncryptionResult {
    private Long cipherPoolId;
    private EncryptionResult encryptionResult;

    /**
     * Creates encryption result
     *
     * @param cipherPoolId     may never be <code>null</code>
     * @param encryptionResult may never be <code>null</code>
     * @throws IllegalArgumentException if one of the arguments is <code>null</code>
     */
    public ScheduleEncryptionResult(Long cipherPoolId, EncryptionResult encryptionResult) {
        if (cipherPoolId == null) {
            throw new IllegalArgumentException("cipher pool id may never be null!");
        }
        if (encryptionResult == null) {
            throw new IllegalArgumentException("encryptionResult may never be null!");
        }
        this.cipherPoolId = cipherPoolId;
        this.encryptionResult = encryptionResult;
    }

    /**
     * @return cipher pool id used for encryption, never <code>null</code>
     */
    public Long getCipherPoolId() {
        return cipherPoolId;
    }

    /**
     * @return encrypted data or <code>null</code>
     */
    public byte[] getEncryptedData() {
        return encryptionResult.getEncryptedData();
    }

    /**
     * @return initial vector, never <code>null</code>
     */
    public InitializationVector getInitialVector() {
        return encryptionResult.getInitialVector();
    }

}