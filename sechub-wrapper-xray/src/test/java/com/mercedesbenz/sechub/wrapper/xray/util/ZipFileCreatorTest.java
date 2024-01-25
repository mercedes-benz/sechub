// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.util;

import static com.mercedesbenz.sechub.test.TestUtil.createTempDirectoryInBuildFolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class ZipFileCreatorTest {

    ZipFileCreator creatorToTest;

    @BeforeEach
    void beforeEach() {
        creatorToTest = new ZipFileCreator();
    }

    @Test
    void createZip_invalid_file_throws_xrayWrapperException() throws IOException {
        /* prepare */
        InputStream testInputStream = mock(InputStream.class);
        File file = new File("");
        when(testInputStream.read(any())).thenReturn(-1);

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> creatorToTest.createZipFromZipInputStream(file, testInputStream));

        /* test */
        assertEquals("Could not save https input stream to zip file", exception.getMessage());
    }

    @Test
    void create_valid_zip_from_ZipInputStream() throws IOException, XrayWrapperException {
        /* prepare */
        File zipFile = new File("src/test/resources/xray-zip-test/xray-zip-test.zip");
        InputStream inputStream = new FileInputStream(zipFile);
        Path target = createTempDirectoryInBuildFolder("xray-zipFileCreator");
        File zipFilename = new File(target + "/test-xray-zip-creator.zip");

        /* execute */
        creatorToTest.createZipFromZipInputStream(zipFilename, inputStream);

        /* test */
        assertTrue(zipFilename.exists());
        assertEquals(zipFile.getTotalSpace(), zipFilename.getTotalSpace());
    }

}