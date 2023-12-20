// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.util;

import static com.mercedesbenz.sechub.test.TestFileReader.loadTextFile;
import static com.mercedesbenz.sechub.test.TestUtil.createTempDirectoryInBuildFolder;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.report.XrayWrapperReportException;

class ZipFileExtractorTest {

    ZipFileExtractor zipFileExtractorToTest;

    @BeforeEach
    void beforeEach() {
        zipFileExtractorToTest = new ZipFileExtractor();
    }

    @Test
    void unzipFile_throws_rayWrapperReportException_with_invalid_file() {
        /* prepare */
        File file = new File("");

        /* execute */
        XrayWrapperReportException exception = assertThrows(XrayWrapperReportException.class,
                () -> zipFileExtractorToTest.unzipFile(file.toPath(), file.toPath()));

        /* test */
        assertEquals("Could not extract zip file.", exception.getMessage());
    }

    @Test
    void unzipFile_with_zip_files() throws IOException, XrayWrapperReportException {
        /* prepare */
        Path target = createTempDirectoryInBuildFolder("xray-zipFileExtractor");
        Path source = Paths.get("src/test/resources/xray-zip-test/xray-zip-test.zip");
        Path expectedFile01 = target.resolve("xray-testfile-01.txt");
        Path expectedFile02 = target.resolve("xray-testfile-02.txt");
        Path expectedFile03 = target.resolve("xray-test-zip-folder/xray-testfile-03.txt");
        Path expectedFileSomeFile = target.resolve("xray-test-zip-folder/.somefile");

        /* execute */
        zipFileExtractorToTest.unzipFile(source, target);

        /* test */
        assertFileExists(expectedFile01);
        assertFileExists(expectedFile02);
        assertFileExists(expectedFile03);
        assertFileExists(expectedFileSomeFile);

        assertEquals("testfile01", loadTextFile(expectedFile01));
        assertEquals("testfile02", loadTextFile(expectedFile02));
        assertEquals("testfile03", loadTextFile(expectedFile03));
        assertEquals("somefile", loadTextFile(expectedFileSomeFile));
    }

    private void assertFileExists(Path path) {
        if (Files.exists(path)) {
            return;
        }
        fail("File does not exists: " + path);
    }
}