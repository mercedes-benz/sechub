package com.mercedesbenz.sechub.commons.core.security;

public interface PersistenceCrypto {
    public String encrypt(String plaintext);

    public String decrypt(String b64Ciphertext);

    /**
     * private String secret;
     *
     * public PersistenceCrypto(String secret) { this.secret = secret; }
     *
     * public String encrypt(String plaintext) { String b64Ciphertext = null; return
     * b64Ciphertext; }
     *
     * public String decrypt(String b64Ciphertext) { String plaintext = null; return
     * plaintext; }
     **/
}
