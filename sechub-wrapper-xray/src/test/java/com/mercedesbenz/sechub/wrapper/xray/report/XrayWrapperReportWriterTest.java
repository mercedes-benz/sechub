package com.mercedesbenz.sechub.wrapper.xray.report;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XrayWrapperReportWriterTest {

    XrayWrapperReportWriter reportWriterToTest;

    @BeforeEach
    void beforeEach() {
        reportWriterToTest = new XrayWrapperReportWriter();
    }

    @Test
    void writeReport_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> reportWriterToTest.writeReport(null, null));
    }

    @Test
    void writeReport_to_empty_file_throws_xrayWrapperReportException() {
        /* prepare */
        Bom sbom = new Bom();
        File file = new File("");

        /* execute + test */
        assertThrows(XrayWrapperReportException.class, () -> reportWriterToTest.writeReport(sbom, file));
    }
}