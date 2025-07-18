package com.mercedesbenz.sechub.wrapper.infralight.product.nmap;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NmapProductImporterTest {

    private NmapProductImporter importerToTest;

    @BeforeEach
    void beforeEach() {
        importerToTest = new NmapProductImporter();
    }

    @Test
    void name_is_defined() {
        assertThat(importerToTest.getProductName()).isNotNull().isNotBlank();
    }

    @Test
    void import_filename_is_defined() {
        assertThat(importerToTest.getImportFileName()).isNotNull().isNotBlank();
    }
}
