// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.security;

import static java.util.Objects.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckSumSupport {

    /**
     * Creates a SHA256 checksum for given file.
     *
     * @param path
     * @return checksum or <code>null</code> when file is not existing
     */
    public String createSha256Checksum(Path path) {
        if (path == null) {
            return null;
        }
        return createSha256Checksum(path.toAbsolutePath().toString());
    }

    /**
     * Creates a SHA256 checksum for given file.
     *
     * @param filepath
     * @return checksum or <code>null</code> when file is not existing
     */
    public String createSha256Checksum(String filepath) {
        if (filepath == null) {
            return null;
        }
        MessageDigest md = createSha256MessageDigest();
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

    public MessageDigest createSha256MessageDigest() {
        MessageDigest md;
        String algorithm = "SHA-256";
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm not supported:" + algorithm);
        }
        return md;
    }

    public boolean hasCorrectSha256ChecksumFile(String checkSum, String filepath) {
        if (checkSum == null) {
            return false;// null is never correct...
        }
        if (filepath == null) {
            return false;
        }
        String calculated = createSha256Checksum(filepath);
        return calculated.equals(checkSum);
    }

    public boolean hasCorrectSha256Checksum(String checkSum, InputStream inputStream) {
        if (checkSum == null) {
            return false;// null is never correct...
        }
        if (inputStream == null) {
            return false;
        }
        String calculated = createSha256Checksum(inputStream);
        if (calculated == null) {
            return false;
        }
        return calculated.equals(checkSum);
    }

    public String convertMessageDigestToHex(MessageDigest digest) {
        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Creates a SHA256 checksum for given file.
     *
     * @param filepath
     * @return checksum or <code>null</code> when file is not existing
     * @throws IOException
     */
    public String createSha256Checksum(InputStream inputStream) {
        requireNonNull(inputStream, "inputStream may not be null");

        MessageDigest digest = createSha256MessageDigest();

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

    public CheckSumValidationResult validateSha256Checksum(String sha256) {
        CheckSumValidationResult result = new CheckSumValidationResult();
        result.message = createSha256ChecksumErrorMessage(sha256);
        if (result.message == null) {
            result.valid = true;
        }
        return result;
    }

    private String createSha256ChecksumErrorMessage(String sha256) {
        if (sha256 == null || sha256.isEmpty()) {
            return "Sha256 checksum not defined";
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
            return "Given checksum is not a valid hex encoded sha256 checksum";
        }

        return null;
    }

    public class CheckSumValidationResult {
        private String message;
        private boolean valid;

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

}
