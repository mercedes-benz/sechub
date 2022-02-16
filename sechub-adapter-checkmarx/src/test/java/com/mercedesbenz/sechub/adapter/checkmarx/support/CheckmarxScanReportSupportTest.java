// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterContext;

public class CheckmarxScanReportSupportTest {
    private CheckmarxScanReportSupport supportToTest;
    private CheckmarxOAuthSupport oauthSupport;
    // FEFF because this is the Unicode char represented by the UTF-8 byte order
    // mark (EF BB BF).
    public static final String UTF8_BOM = "\uFEFF";

    @Before
    public void before() throws Exception {
        supportToTest = new CheckmarxScanReportSupport();
        oauthSupport = mock(CheckmarxOAuthSupport.class);
    }

    @Test
    public void fetchReportResult__support_does_use_result_as_is_when_empty() throws Exception {
        /* prepare */
        CheckmarxAdapterContext context = prepareContent("");

        /* execute */
        supportToTest.fetchReportResult(oauthSupport, context);

        /* test */
        verify(context).setResult("");
    }

    @Test
    public void fetchReportResult__support_does_remove_existing_byte_order_marks() throws Exception {
        /* prepare */
        CheckmarxAdapterContext context = prepareContent(UTF8_BOM + "<?xml bla");

        /* execute */
        supportToTest.fetchReportResult(oauthSupport, context);

        /* test */
        verify(context).setResult("<?xml bla");
    }

    @Test
    public void fetchReportResult__support_does_remove_nothing_when_plain_xml() throws Exception {
        /* prepare */
        CheckmarxAdapterContext context = prepareContent("<?xml bla");

        /* execute */
        supportToTest.fetchReportResult(oauthSupport, context);

        /* test */
        verify(context).setResult("<?xml bla");
    }

    private CheckmarxAdapterContext prepareContent(String content) {
        ResponseEntity<String> entity = new ResponseEntity<>(content, HttpStatus.OK);

        RestOperations restOperations = mock(RestOperations.class);
        CheckmarxAdapterContext context = mock(CheckmarxAdapterContext.class);

        when(context.getRestOperations()).thenReturn(restOperations);
        when(context.getAPIURL(any())).thenReturn("path");
        when(restOperations.getForEntity(eq("path"), eq(String.class))).thenReturn(entity);
        return context;
    }

}
