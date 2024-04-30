// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckSumSupportTest {

    private CheckSumSupport serviceToTest;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new CheckSumSupport();
    }

    @Test
    void test_simple_sha256_calculation_works() throws Exception {
        /* prepare */
        InputStream inputStream = createFileInputStreamToTestZipfile();

        /* execute */
        String checksum = serviceToTest.createSha256Checksum(inputStream);

        /* test */
        assertEquals("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", checksum);

        inputStream.close();
    }

    @Test
    void test_sha256_sum_digest_creation_works() {
        MessageDigest digest = serviceToTest.createSha256MessageDigest();

        assertNotNull(digest);
    }

    @Test
    void test_simple_sha256_check_works() throws Exception {
        /* prepare */
        InputStream inputStream1 = createFileInputStreamToTestZipfile();
        InputStream inputStream2 = createFileInputStreamToTestZipfile();
        InputStream inputStream3 = createFileInputStreamToTestZipfile();

        /* execute */
        assertTrue(serviceToTest.hasCorrectSha256Checksum("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", inputStream1));
        assertTrue(serviceToTest.hasCorrectSha256Checksum("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", inputStream2));
        assertFalse(serviceToTest.hasCorrectSha256Checksum("19060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", inputStream3));

        inputStream1.close();
        inputStream2.close();
        inputStream3.close();

    }

    private InputStream createFileInputStreamToTestZipfile() throws FileNotFoundException {
        File file = new File("./src/test/resources/zipfile_contains_only_test1.txt.zip");

        if (!file.exists()) {
            throw new IllegalStateException("File does not exist: " + file);
        }

        String absolutePath = file.getAbsolutePath();

        InputStream inputStream = new FileInputStream(new File(absolutePath));
        return inputStream;
    }
}
