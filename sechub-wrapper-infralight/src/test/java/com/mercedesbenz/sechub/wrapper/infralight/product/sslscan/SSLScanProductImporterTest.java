package com.mercedesbenz.sechub.wrapper.infralight.product.sslscan;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SSLScanProductImporterTest {

    private SSLScanProductImporter importerToTest;

    @BeforeEach
    void beforeEach() {
        importerToTest = new SSLScanProductImporter();
    }

    @Test
    void name_is_defined() {
        assertThat(importerToTest.getName()).isNotNull().isNotBlank();
    }

    @Test
    void import_filename_is_defined() {
        assertThat(importerToTest.getImportFileName()).isNotNull().isNotBlank();
    }

}
