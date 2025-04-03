// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

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

    @ParameterizedTest
    @EnumSource(ProductIdentifier.class)
    void build__uses_product_identifier_scan_type_and_provides_data(ProductIdentifier productIdentifier) {
        /* prepare */
        SecHubMessage info = new SecHubMessage(SecHubMessageType.INFO, "info");
        String importId = "id1";
        String importData = "{}";
        String productId = productIdentifier.name();
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
        assertEquals(importParameter.getScanType(), productIdentifier.getType());
    }

}
