// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.SharedKernelTestFileSupport;

public class FileChecksumSHA256ServiceTest {

    private FileChecksumSHA256Service serviceToTest;

    @Before
    public void before() {
        serviceToTest = new FileChecksumSHA256Service();
    }

    @Test
    public void test_simple_sha256_calculation_works() throws Exception {
        /* prepare */
        SharedKernelTestFileSupport testfileSupport = SharedKernelTestFileSupport.getTestfileSupport();
        String absolutePath = testfileSupport.createFileFromResourcePath("zipfile_contains_only_test1.txt.zip").getAbsolutePath();

        /* execute */
        String checksum = serviceToTest.createChecksum(absolutePath);

        /* test */
        assertEquals("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", checksum);
        assertEquals("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", checksum);
    }

    @Test
    public void test_simple_sha256_check_works() throws Exception {
        /* prepare */
        SharedKernelTestFileSupport testfileSupport = SharedKernelTestFileSupport.getTestfileSupport();
        String absolutePath = testfileSupport.createFileFromResourcePath("zipfile_contains_only_test1.txt.zip").getAbsolutePath();

        /* execute */
        assertTrue(serviceToTest.hasCorrectChecksum("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", absolutePath));
        assertTrue(serviceToTest.hasCorrectChecksum("59060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", absolutePath));
        assertFalse(serviceToTest.hasCorrectChecksum("19060b6b4e8d137596dc01ec15d5da1ab4c4ad0d756c780ed88225f082ae87b7", absolutePath));

    }

}
