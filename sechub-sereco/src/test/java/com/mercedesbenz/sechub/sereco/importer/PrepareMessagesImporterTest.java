package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.sereco.ImportParameter;

class PrepareMessagesImporterTest {

    @Test
    void isForSecurityProduct_interface_default_is_true() {
        /* prepare */
        PrepareMessagesImporter importerToTest = spy(PrepareMessagesImporter.class);

        /* execute + test */
        assertFalse(importerToTest.isForSecurityProduct()); // default implementation overriden
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void empty_or_null__import_data_cannot_be_imported(String importData) {
        /* prepare */
        PrepareMessagesImporter importerToTest = new PrepareMessagesImporter();

        ImportParameter param = mock(ImportParameter.class);
        when(param.getImportData()).thenReturn(importData);

        /* execute + test */
        assertEquals(ProductImportAbility.PRODUCT_FAILED_OR_CANCELED, importerToTest.isAbleToImportForProduct(param));
    }

    @ParameterizedTest
    @ValueSource(strings = { "prepared", "{}" })
    void existing_but_not_accepted__import_data_cannot_be_imported(String importData) {
        /* prepare */
        PrepareMessagesImporter importerToTest = new PrepareMessagesImporter();

        ImportParameter param = mock(ImportParameter.class);
        when(param.getImportData()).thenReturn(importData);

        /* execute + test */
        assertEquals(ProductImportAbility.NOT_ABLE_TO_IMPORT, importerToTest.isAbleToImportForProduct(param));
    }

    @Test
    void containing_key_word_import_data_can_be_imported() {
        /* prepare */
        PrepareMessagesImporter importerToTest = new PrepareMessagesImporter();

        ImportParameter param = mock(ImportParameter.class);
        when(param.getImportData()).thenReturn("SECHUB_PREPARE_DONE");

        /* execute + test */
        assertEquals(ProductImportAbility.ABLE_TO_IMPORT, importerToTest.isAbleToImportForProduct(param));
    }

}
