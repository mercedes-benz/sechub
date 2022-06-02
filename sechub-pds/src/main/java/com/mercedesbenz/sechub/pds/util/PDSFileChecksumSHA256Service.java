// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.PDSBadRequestException;

@Service
public class PDSFileChecksumSHA256Service {

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
        if (filepath == null) {
            return null;
        }
        MessageDigest md = createSHA256MessageDigest();
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

    public String convertMessageDigestToHex(MessageDigest digest) {
        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public MessageDigest createSHA256MessageDigest() {
        MessageDigest md;
        String algorithm = "SHA-256";
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm not supported:" + algorithm);
        }
        return md;
    }

    public void assertValidSha256Checksum(String sha256) {
        if (sha256 == null || sha256.isEmpty()) {
            throw new PDSBadRequestException("Sha256 checksum not defined");
        }
        for (char c : sha256.toLowerCase().toCharArray()) {
            if (Character.isDigit(c)) {
                continue;
            }
            switch (c) {
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                /* everything fine */
                continue;
            }
            throw new PDSBadRequestException("Given checksum is not a valid hex encoded sha256 checksum");
        }

    }

}
