package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.ZipFileExtractor.unzipFile;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.report.XrayWrapperReportException;

class ZipFileExtractorTest {

    @Test
    void unzipFile_throws_xrayWrapperReportException() {
        /* execute + test */
        assertThrows(XrayWrapperReportException.class, () -> unzipFile(null, null));
    }

    @Test
    void unzipFile_throws_rayWrapperReportException_with_invalid_file() {
        /* prepare */
        File file = new File("file");

        /* execute + test */
        assertThrows(XrayWrapperReportException.class, () -> unzipFile(file.toPath(), file.toPath()));
    }
}