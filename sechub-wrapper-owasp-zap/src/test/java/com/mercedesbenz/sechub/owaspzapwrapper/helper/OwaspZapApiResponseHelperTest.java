// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;

class OwaspZapApiResponseHelperTest {

    private OwaspZapApiResponseHelper helperToTest;

    @BeforeEach
    void beforeEach() {
        helperToTest = new OwaspZapApiResponseHelper();
    }

    @Test
    void invalid_type_helper_throws_mustexitruntimeexception() {
        /* prepare */
        ApiResponse response = new ApiResponseList("example");

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> helperToTest.getIdOfApiRepsonse(response));
    }

    @Test
    void valid_type_results_in_correct_id() {
        /* prepare */
        ApiResponse response = new ApiResponseElement("example", "10");

        /* execute */
        String id = helperToTest.getIdOfApiRepsonse(response);

        /* test */
        assertEquals("10", id);
    }
}
