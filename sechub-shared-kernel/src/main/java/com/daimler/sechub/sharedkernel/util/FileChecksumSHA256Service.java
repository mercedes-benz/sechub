// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.util;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

@Service
public class FileChecksumSHA256Service {

    public boolean hasCorrectChecksum(String checkSum, String filepath) {
        if (checkSum == null) {
            return false;// null is never correct...
        }
        if (filepath == null) {
            return false;
        }
        String calculated = createChecksum(filepath);
        return calculated.equals(checkSum);
    }

    /**
     * Creates a SHA256 checksum for given file.
     *
     * @param filepath
     * @return checksum or <code>null</code> when file is not existing
     * @throws IOException
     */
    public String createChecksum(String filepath) {
        notNull(filepath, "filepath may not be null");
        MessageDigest md;
        String algorithm = "SHA-256";
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm not supported:" + algorithm);
        }
        // file hashing with DigestInputStream
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
            while (dis.read() != -1)
                ; // empty loop to clear the data
            md = dis.getMessageDigest();
        } catch (IOException e) {
            return null;
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }

}
