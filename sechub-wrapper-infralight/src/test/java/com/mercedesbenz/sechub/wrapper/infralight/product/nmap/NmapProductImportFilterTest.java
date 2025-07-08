package com.mercedesbenz.sechub.wrapper.infralight.product.nmap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImporter;

class NmapProductImportFilterTest {

    private NmapProductImportFilter importerToTest;

    @BeforeEach
    void beforeEach() {
        importerToTest = new NmapProductImportFilter();
    }

    @Test
    void can_filter_NmapProductImporter() {
        assertThat(importerToTest.canFilter(new NmapProductImporter())).isTrue();
    }

    @Test
    void cannot_filter_a_mocked_importer() {
        /* prepare */
        InfralightProductImporter importer = mock(InfralightProductImporter.class);

        /* execute + test */
        assertThat(importerToTest.canFilter(importer)).isFalse();
    }

}
