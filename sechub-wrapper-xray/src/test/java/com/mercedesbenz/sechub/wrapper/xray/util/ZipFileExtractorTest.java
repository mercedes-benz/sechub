package com.mercedesbenz.sechub.wrapper.xray.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

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
    void unzipFile_throws_xrayWrapperReportException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> zipFileExtractorToTest.unzipFile(null, null));
    }

    @Test
    void unzipFile_throws_rayWrapperReportException_with_invalid_file() {
        /* prepare */
        File file = new File("file");

        /* execute + test */
        assertThrows(XrayWrapperReportException.class, () -> zipFileExtractorToTest.unzipFile(file.toPath(), file.toPath()));
    }
}