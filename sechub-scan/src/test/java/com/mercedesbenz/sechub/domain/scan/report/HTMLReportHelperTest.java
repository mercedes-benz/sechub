// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

class HTMLReportHelperTest {

    private HTMLReportHelper helperToTest;

    @BeforeEach
    void beforeEach() {
        helperToTest = new HTMLReportHelper();
    }

    @ParameterizedTest
    @EnumSource(SecHubMessageType.class)
    void getMessageTypeAsHTMLIcon_by_wellknown_message_type_results_in_unicode_icon(SecHubMessageType type) {
        /* execute */
        String icon = helperToTest.getMessageTypeAsHTMLIcon(type);

        /* test */
        switch (type) {
        case ERROR:
            assertEquals("&#128711;", icon); // prohibit
            break;
        case INFO:
            assertEquals("&#128712;", icon); // circle info
            break;
        case WARNING:
            assertEquals("&#9888;", icon); // warning
            break;
        default:
            fail("Unexpected type - new ?");

        }
    }

    @ParameterizedTest
    @NullSource
    void getMessageTypeAsHTMLIcon_by_unknown_message_type_results_in_empty_string(SecHubMessageType type) {
        /* execute */
        String icon = helperToTest.getMessageTypeAsHTMLIcon(type);

        /* test */
        assertEquals("", icon);
    }

}
