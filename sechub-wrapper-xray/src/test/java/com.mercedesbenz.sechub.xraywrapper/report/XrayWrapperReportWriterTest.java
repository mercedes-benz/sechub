package com.mercedesbenz.sechub.xraywrapper.report;

import static com.mercedesbenz.sechub.xraywrapper.report.XrayWrapperReportWriter.writeReport;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.Test;

class XrayWrapperReportWriterTest {

    @Test
    public void test_writeReport_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> writeReport(null, null));
    }

    @Test
    public void test_writeReport_XrayWrapperReportException() {
        /* prepare */
        Bom sbom = new Bom();
        File file = new File("");

        /* execute + test */
        assertThrows(XrayWrapperReportException.class, () -> writeReport(sbom, file));
    }
}