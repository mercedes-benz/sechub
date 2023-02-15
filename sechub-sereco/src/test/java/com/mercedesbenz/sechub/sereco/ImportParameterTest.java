// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

public class ImportParameterTest {
    @Test
    void build__no_values() {
        /* prepare + execute */
        ImportParameter importParameter = ImportParameter.builder().build();

        /* test */
        assertNotNull(importParameter);
        assertNull(importParameter.getImportData());
        assertNull(importParameter.getImportId());
        assertNull(importParameter.getProductId());
        assertNull(importParameter.getProductMessages());
        assertEquals(importParameter.getScanType(), ScanType.UNKNOWN);
    }

    @Test
    void build__from_data_id_productmessages_productid() {
        /* prepare */
        SecHubMessage info = new SecHubMessage(SecHubMessageType.INFO, "info");
        String importId = "id1";
        String importData = "{}";
        String productId = "PDS_CODESCAN";
        List<SecHubMessage> messages = List.of(info);

        /* execute */
        /* formatter:off */
        ImportParameter importParameter = ImportParameter.builder().importData(importData).importId(importId).importProductMessages(messages)
                .productId(productId).build();
        /* formatter:on */

        /* test */
        assertEquals(importData, importParameter.getImportData());
        assertEquals(importId, importParameter.getImportId());
        assertEquals(productId, importParameter.getProductId());
        assertEquals(1, importParameter.getProductMessages().size());
        assertEquals(importParameter.getScanType(), ScanType.CODE_SCAN);
    }

    @Test
    void build__productid_scancode() {
        /* prepare */
        String productId = "PDS_CODESCAN";

        /* execute */
        ImportParameter importParameter = ImportParameter.builder().productId(productId).build();

        /* test */
        assertEquals(importParameter.getScanType(), ScanType.CODE_SCAN);
    }

    @Test
    void build__productid_checkmarx() {
        /* prepare */
        String productId = "CHECKMARX";

        /* execute */
        ImportParameter importParameter = ImportParameter.builder().productId(productId).build();

        /* test */
        assertEquals(importParameter.getScanType(), ScanType.CODE_SCAN);
    }

    @Test
    void build__productid_nessus() {
        /* prepare */
        String productId = "NESSUS";

        /* execute */
        ImportParameter importParameter = ImportParameter.builder().productId(productId).build();

        /* test */
        assertEquals(importParameter.getScanType(), ScanType.INFRA_SCAN);
    }

    @Test
    void build__productid_pds_infrascan() {
        /* prepare */
        String productId = "PDS_INFRASCAN";

        /* execute */
        ImportParameter importParameter = ImportParameter.builder().productId(productId).build();

        /* test */
        assertEquals(importParameter.getScanType(), ScanType.INFRA_SCAN);
    }

    @Test
    void build__productid_netsparker() {
        /* prepare */
        String productId = "NETSPARKER";

        /* execute */
        ImportParameter importParameter = ImportParameter.builder().productId(productId).build();

        /* test */
        assertEquals(importParameter.getScanType(), ScanType.WEB_SCAN);
    }

    @Test
    void build__productid_pds_webscan() {
        /* prepare */
        String productId = "PDS_WEBSCAN";

        /* execute */
        ImportParameter importParameter = ImportParameter.builder().productId(productId).build();

        /* test */
        assertEquals(importParameter.getScanType(), ScanType.WEB_SCAN);
    }

    @Test
    void build__productid_pds_licensescan() {
        /* prepare */
        String productId = "PDS_LICENSESCAN";

        /* execute */
        ImportParameter importParameter = ImportParameter.builder().productId(productId).build();

        /* test */
        assertEquals(importParameter.getScanType(), ScanType.LICENSE_SCAN);
    }
}
