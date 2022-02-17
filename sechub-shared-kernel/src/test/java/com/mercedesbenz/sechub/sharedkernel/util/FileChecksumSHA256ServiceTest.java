// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.sharedkernel.SharedKernelTestFileSupport;

public class FileChecksumSHA256ServiceTest {

    private FileChecksumSHA256Service serviceToTest;

    @Before
    public void before() {
        serviceToTest = new FileChecksumSHA256Service();
    }

    @Test
    public void test_simple_sha256_calculation_works() throws Exception {
        /* prepare */
        InputStream inputStream = createFileInputStreamToTestZipfile();

        /* execute */
        String checksum = serviceToTest.createChecksum(inputStream);

        /* test */
        assertEquals("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", checksum);
        assertEquals("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", checksum);

        inputStream.close();
    }

    private InputStream createFileInputStreamToTestZipfile() throws FileNotFoundException {
        SharedKernelTestFileSupport testfileSupport = SharedKernelTestFileSupport.getTestfileSupport();
        String absolutePath = testfileSupport.createFileFromResourcePath("zipfile_contains_only_test1.txt.zip").getAbsolutePath();

        InputStream inputStream = new FileInputStream(new File(absolutePath));
        return inputStream;
    }

    @Test
    public void test_simple_sha256_check_works() throws Exception {
        /* prepare */
        InputStream inputStream1 = createFileInputStreamToTestZipfile();
        InputStream inputStream2 = createFileInputStreamToTestZipfile();
        InputStream inputStream3 = createFileInputStreamToTestZipfile();

        /* execute */
        assertTrue(serviceToTest.hasCorrectChecksum("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", inputStream1));
        assertTrue(serviceToTest.hasCorrectChecksum("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", inputStream2));
        assertFalse(serviceToTest.hasCorrectChecksum("19060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", inputStream3));

        inputStream1.close();
        inputStream2.close();
        inputStream3.close();

    }

}
