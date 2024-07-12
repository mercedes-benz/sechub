// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

public class EncryptionRotationSetup {

    public static RotationSetupBuilder builder() {
        return new RotationSetupBuilder();
    }

    /* private, because MUST be created by builder - which ensures setup is valid */
    private EncryptionRotationSetup() {

    }

    private byte[] encryptedDataToRotate;
    private PersistentCipher oldCipher;
    private InitializationVector oldInitialVector;
    private PersistentCipher newCipher;
    private InitializationVector newInitialVector;

    public byte[] getEncryptedDataToRotate() {
        return encryptedDataToRotate;
    }

    public PersistentCipher getOldCipher() {
        return oldCipher;
    }

    public InitializationVector getOldInitialVector() {
        return oldInitialVector;
    }

    public PersistentCipher getNewCipher() {
        return newCipher;
    }

    public InitializationVector getNewInitialVector() {
        return newInitialVector;
    }

    public static class RotationSetupBuilder {

        private byte[] encryptedDataToRotate;
        private PersistentCipher oldCipher;
        private InitializationVector oldInitialVector;
        private PersistentCipher newCipher;
        private InitializationVector newInitialVector;

        private RotationSetupBuilder() {
        }

        public RotationSetupBuilder oldData(byte[] encryptedDataToRotate) {
            this.encryptedDataToRotate = encryptedDataToRotate;
            return this;
        }

        public RotationSetupBuilder oldInitialVector(InitializationVector oldInitialVector) {
            this.oldInitialVector = oldInitialVector;
            return this;
        }

        public RotationSetupBuilder newInitialVector(InitializationVector newInitialVector) {
            this.newInitialVector = newInitialVector;
            return this;
        }

        public RotationSetupBuilder oldCipher(PersistentCipher oldCipher) {
            this.oldCipher = oldCipher;
            return this;
        }

        public RotationSetupBuilder newCipher(PersistentCipher newCipher) {
            this.newCipher = newCipher;
            return this;
        }

        /**
         * Builds a new rotation data object. If the new initial vector is not set, the
         * old initial vector will be used If the new cipher is not set, the old cipher
         * will be used. But if none of them ( no new initial vector and no new cipher)
         * is defined an {@link IllegalArgumentException} will be thrown (because setup
         * provides no rotation at all)
         *
         * @return new rotation data object
         * @throws IllegalArgumentException when no new cipher and no new initial vector
         *                                  defined
         * @throws IllegalArgumentException when no old cipher defined
         * @throws IllegalArgumentException when no old initial vector defined
         */
        public EncryptionRotationSetup build() {
            if (oldCipher == null) {
                throw new IllegalArgumentException("old cipher must be defined in builder before build is called!");
            }
            if (oldInitialVector == null) {
                throw new IllegalArgumentException("old initial vector must be defined in builder before build is called!");
            }

            if (newInitialVector == null && newCipher == null) {
                throw new IllegalArgumentException("no new cipher or a new initial vector given - rotation is impossible in this case!");
            }

            EncryptionRotationSetup data = new EncryptionRotationSetup();
            data.encryptedDataToRotate = encryptedDataToRotate;
            data.oldCipher = this.oldCipher;
            data.oldInitialVector = this.oldInitialVector;

            if (newCipher == null) {
                data.newCipher = this.oldCipher;
            } else {
                data.newCipher = this.newCipher;
            }

            if (newInitialVector == null) {
                data.newInitialVector = this.oldInitialVector;
            } else {
                data.newInitialVector = this.newInitialVector;
            }

            return data;
        }
    }
}
