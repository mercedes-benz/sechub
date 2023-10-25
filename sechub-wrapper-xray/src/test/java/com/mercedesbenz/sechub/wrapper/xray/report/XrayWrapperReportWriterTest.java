package com.mercedesbenz.sechub.wrapper.xray.report;

import static com.mercedesbenz.sechub.wrapper.xray.report.XrayWrapperReportWriter.writeReport;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.Test;

class XrayWrapperReportWriterTest {

    @Test
    void writeReport_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> writeReport(null, null));
    }

    @Test
    void writeReport_throws_xrayWrapperReportException() {
        /* prepare */
        Bom sbom = new Bom();
        File file = new File("");

        /* execute + test */
        assertThrows(XrayWrapperReportException.class, () -> writeReport(sbom, file));
    }
}