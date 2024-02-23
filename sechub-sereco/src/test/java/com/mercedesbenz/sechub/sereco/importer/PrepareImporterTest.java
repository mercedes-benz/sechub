package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.core.prepare.PrepareConstants;
import com.mercedesbenz.sechub.sereco.ImportParameter;

class PrepareImporterTest {

    @Test
    void isForSecurityProduct_interface_default_is_true() {
        /* prepare */
        PrepareImporter importerToTest = spy(PrepareImporter.class);

        /* execute + test */
        assertFalse(importerToTest.isForSecurityProduct()); // default implementation overriden
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void empty_or_null__import_data_cannot_be_imported(String importData) {
        /* prepare */
        PrepareImporter importerToTest = new PrepareImporter();

        ImportParameter param = mock(ImportParameter.class);
        when(param.getImportData()).thenReturn(importData);

        /* execute + test */
        assertFalse(importerToTest.isAbleToImportForProduct(param));
    }

    @ParameterizedTest
    @ValueSource(strings = { "prepared", "{}" })
    void existing_but_not_accepted__import_data_cannot_be_imported(String importData) {
        /* prepare */
        PrepareImporter importerToTest = new PrepareImporter();

        ImportParameter param = mock(ImportParameter.class);
        when(param.getImportData()).thenReturn(importData);

        /* execute + test */
        assertFalse(importerToTest.isAbleToImportForProduct(param));
    }

    @Test
    void containing_key_word_import_data_can_be_imported() {
        /* prepare */
        PrepareImporter importerToTest = new PrepareImporter();

        ImportParameter param = mock(ImportParameter.class);
        when(param.getImportData()).thenReturn(PrepareConstants.SECHUB_PREPARE_RESULT);

        /* execute + test */
        assertTrue(importerToTest.isAbleToImportForProduct(param));
    }

}
