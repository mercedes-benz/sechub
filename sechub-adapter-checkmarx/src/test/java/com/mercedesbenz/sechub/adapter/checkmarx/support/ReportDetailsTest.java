package com.mercedesbenz.sechub.adapter.checkmarx.support;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReportDetailsTest {
    ReportDetails reportDetailsToTest;

    @BeforeEach
    void before() throws Exception {
        reportDetailsToTest = new ReportDetails();
    }

    @Test
    void isRunning__not_found_false() {
        reportDetailsToTest.notFound = false;

        assertTrue(reportDetailsToTest.isRunning());
    }

    @Test
    void isRunning__not_found_true() {
        reportDetailsToTest.notFound = true;

        assertFalse(reportDetailsToTest.isRunning());
    }

    @Test
    void isRunning__not_found_false_and_created() {
        reportDetailsToTest.notFound = false;
        reportDetailsToTest.status = "Created";

        assertFalse(reportDetailsToTest.isRunning());
    }

    @Test
    void isRunning__not_found_false_and_failed() {
        reportDetailsToTest.notFound = false;
        reportDetailsToTest.status = "Failed";

        assertFalse(reportDetailsToTest.isRunning());
    }
}
