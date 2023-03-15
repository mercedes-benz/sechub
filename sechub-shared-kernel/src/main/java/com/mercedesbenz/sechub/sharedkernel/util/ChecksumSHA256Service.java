// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.util;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

@Service
public class ChecksumSHA256Service {

    public boolean hasCorrectChecksum(String checkSum, InputStream inputStream) {
        if (checkSum == null) {
            return false;// null is never correct...
        }
        if (inputStream == null) {
            return false;
        }
        String calculated = createChecksum(inputStream);
        if (calculated == null) {
            return false;
        }
        return calculated.equals(checkSum);
    }

    /**
     * Creates a SHA256 checksum for given file.
     *
     * @param filepath
     * @return checksum or <code>null</code> when file is not existing
     * @throws IOException
     */
    public String createChecksum(InputStream inputStream) {
        notNull(inputStream, "inputStream may not be null");
        MessageDigest digest = createSHA256MessageDigest();

        // file hashing with DigestInputStream
        try (DigestInputStream dis = new DigestInputStream(inputStream, digest)) {
            while (dis.read() != -1)
                ; // empty loop to clear the data
            digest = dis.getMessageDigest();
        } catch (IOException e) {
            return null;
        }

        return convertMessageDigestToHex(digest);

    }

    public String convertMessageDigestToHex(MessageDigest digest) {
        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public MessageDigest createSHA256MessageDigest() {
        MessageDigest digest;
        String algorithm = "SHA-256";
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm not supported:" + algorithm);
        }
        return digest;
    }

}
