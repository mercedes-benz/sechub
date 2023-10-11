package com.mercedesbenz.sechub.xraywrapper.report;

import static com.mercedesbenz.sechub.xraywrapper.report.XrayWrapperReportWriter.writeReport;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class XrayWrapperReportWriterTest {

    @Test
    public void test_writeReport_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> writeReport(null, null));
    }
}