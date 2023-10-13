package com.mercedesbenz.sechub.xraywrapper.util;

import com.mercedesbenz.sechub.xraywrapper.report.XrayWrapperReportException;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.mercedesbenz.sechub.xraywrapper.util.ZipFileExtractor.unzipFile;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZipFileExtractorTest {

    @Test
    public void test_unzipFile_null() {
        /* execute + test */
        assertThrows(XrayWrapperReportException.class, () -> unzipFile(null, null));
    }

    @Test
    public void test_unzipFile_invalidFile() {
        /* prepare */
        File file = new File("file");

        /* execute + test */
        assertThrows(XrayWrapperReportException.class, () -> unzipFile(file.toPath(), file.toPath()));
    }

}