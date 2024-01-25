// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void writeReport_null_SBOM_throws_illegalStateException() {
        /* Prepare */
        File file = new File("");

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> reportWriterToTest.writeReport(null, file));

        /* test */
        assertEquals("SBOM or report file can not be NULL", exception.getMessage());
    }

    @Test
    void writeReport_null_filename_throws_illegalStateException() {
        /* Prepare */
        Bom sbom = new Bom();

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> reportWriterToTest.writeReport(sbom, null));

        /* test */
        assertEquals("SBOM or report file can not be NULL", exception.getMessage());
    }

    @Test
    void writeReport_to_empty_filename_throws_xrayWrapperReportException() {
        /* prepare */
        Bom sbom = new Bom();
        File file = new File("");

        /* execute */
        XrayWrapperReportException exception = assertThrows(XrayWrapperReportException.class, () -> reportWriterToTest.writeReport(sbom, file));

        /* test */
        assertEquals("Could not write final Xray report to file", exception.getMessage());
    }
}