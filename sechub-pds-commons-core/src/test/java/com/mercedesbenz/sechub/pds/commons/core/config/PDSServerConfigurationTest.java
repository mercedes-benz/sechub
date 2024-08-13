// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.commons.core.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.test.TestFileReader;

class PDSServerConfigurationTest {

    @Test
    void a_correct_server_configuration_file_can_be_read_as_json() throws Exception {

        /* prepare */
        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/config/pds-config-example1.json"));

        /* execute */
        PDSServerConfiguration configuration = PDSServerConfiguration.fromJSON(json);

        /* test */
        assertEquals("unique_id_for_server_or_cluster", configuration.getServerId());
        List<PDSProductSetup> products = configuration.getProducts();
        assertEquals(2, products.size());
        Iterator<PDSProductSetup> it = products.iterator();

        PDSProductSetup product1 = it.next();
        PDSProductSetup product2 = it.next();

        assertEquals("PRODUCT_1", product1.getId());
        assertEquals("/srv/security/scanner1.sh", product1.getPath());
        assertEquals(ScanType.CODE_SCAN, product1.getScanType());
        assertNotNull(product1.getDescription());

        PDSProductParameterSetup params = product1.getParameters();
        List<PDSProductParameterDefinition> mandatory = params.getMandatory();
        assertEquals(2, mandatory.size());

        assertEquals("PRODUCT_2", product2.getId());
        assertEquals("/srv/security/scanner2.sh", product2.getPath());
        assertEquals(ScanType.INFRA_SCAN, product2.getScanType());
    }

    @Test
    void a_server_configuration_with_unknown_entries_can_be_read() throws Exception {
        /* prepare */
        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/config/pds-config-example2-with-unknown-parts.json"));

        /* execute */
        PDSServerConfiguration configuration = PDSServerConfiguration.fromJSON(json);

        /* test */
        assertEquals("example_with_unknown_parts", configuration.getServerId());
    }

    @Test
    void example_configuration_from_documentation_can_be_loaded() throws Exception {
        /* prepare */
        String json = TestFileReader
                .readTextFromFile(new File("./..//sechub-doc/src/docs/asciidoc/documents/pds/product_delegation_server_config_example1.json"));

        /* execute */
        PDSServerConfiguration configuration = PDSServerConfiguration.fromJSON(json);

        /* test */
        assertEquals("UNIQUE_SERVER_ID", configuration.getServerId());
    }

}
