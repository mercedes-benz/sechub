// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

public class PersistentCipherFactory {

    public PersistentCipher createCipher(SecretKeyProvider secretProvider, PersistentCipherType type) {

        if (secretProvider == null && !PersistentCipherType.NONE.equals(type)) {
            throw new IllegalArgumentException("secret key provider must be defined for all cipher types (except NONE)");
        }

        if (type == null) {
            throw new IllegalArgumentException("cipher type must be defined!");
        }

        switch (type) {
        case AES_GCM_SIV_128:
            assertKeyHasBits(secretProvider, type, 128);
            return new AesGcmSivCipher(secretProvider.getSecretKey(), type);
        case AES_GCM_SIV_256:
            assertKeyHasBits(secretProvider, type, 256);
            return new AesGcmSivCipher(secretProvider.getSecretKey(), type);
        case NONE:
            return new NoneCipher();
        default:
            throw new IllegalStateException("Ther is no implementation for %s".formatted(type));
        }
    }

    private static void assertKeyHasBits(SecretKeyProvider secretProvider, PersistentCipherType type, int bitsWanted) {
        int amountOfBits = secretProvider.getLengthOfSecretInBits();
        if (amountOfBits != bitsWanted) {
            throw new IllegalArgumentException("The type %s does only accept %s bits for secret key, but returned secret key by provider had %s bit!"
                    .formatted(type, bitsWanted, amountOfBits));
        }
    }

}
