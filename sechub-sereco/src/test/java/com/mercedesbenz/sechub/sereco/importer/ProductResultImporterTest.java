package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class ProductResultImporterTest {

    @Test
    void isForSecurityProduct_interface_default_is_true() {
        /* prepare */
        ProductResultImporter toTest = spy(ProductResultImporter.class);

        /* execute + test */
        assertTrue(toTest.isForSecurityProduct()); // default implementation returns true
    }

}
