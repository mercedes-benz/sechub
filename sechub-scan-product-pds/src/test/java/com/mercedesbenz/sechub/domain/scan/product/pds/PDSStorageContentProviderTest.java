// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.storage.core.JobStorage;

class PDSStorageContentProviderTest {

    private JobStorage storage;
    private SecHubConfigurationModel model;
    private SecHubConfigurationModelSupport modelSupport;

    @BeforeEach
    void beforeEach() {
        storage = mock(JobStorage.class);
        model = mock(SecHubConfigurationModel.class);
        modelSupport = mock(SecHubConfigurationModelSupport.class);
    }

    @ParameterizedTest
    @ArgumentsSource(RequiredForScanTypeArgumentsProvider.class)
    void isBinaryRequired_storage_not_reused_is_handled_by_modelsupport_result(ScanType scanType, boolean required) {
        /* prepare */
        when(modelSupport.isBinaryRequired(scanType, model)).thenReturn(required);
        PDSStorageContentProvider providerToTest = new PDSStorageContentProvider(storage, false, scanType, modelSupport, model);

        /* execute */
        assertEquals(required, providerToTest.isBinaryRequired());
    }

    @ParameterizedTest
    @ArgumentsSource(RequiredForScanTypeArgumentsProvider.class)
    void isSourceRequired_storage_not_reused__is_handled_by_modelsupport_result(ScanType scanType, boolean required) {
        /* prepare */
        when(modelSupport.isSourceRequired(scanType, model)).thenReturn(required);
        PDSStorageContentProvider providerToTest = new PDSStorageContentProvider(storage, false, scanType, modelSupport, model);

        /* execute */
        assertEquals(required, providerToTest.isSourceRequired());
    }

    @ParameterizedTest
    @ArgumentsSource(RequiredForScanTypeArgumentsProvider.class)
    void isBinaryRequired_storage_reused_is_handled_by_modelsupport_result(ScanType scanType, boolean required) {
        /* prepare */
        when(modelSupport.isBinaryRequired(scanType, model)).thenReturn(required);
        PDSStorageContentProvider providerToTest = new PDSStorageContentProvider(storage, true, scanType, modelSupport, model);

        /* execute */
        assertEquals(required, providerToTest.isBinaryRequired());
    }

    @ParameterizedTest
    @ArgumentsSource(RequiredForScanTypeArgumentsProvider.class)
    void isSourceRequired_storage_reused__is_handled_by_modelsupport_result(ScanType scanType, boolean required) {
        /* prepare */
        when(modelSupport.isSourceRequired(scanType, model)).thenReturn(required);
        PDSStorageContentProvider providerToTest = new PDSStorageContentProvider(storage, true, scanType, modelSupport, model);

        /* execute */
        assertEquals(required, providerToTest.isSourceRequired());
    }

    private static class RequiredForScanTypeArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              /* Arguments: scanType, required (simulated result from model support) */
              Arguments.of(ScanType.CODE_SCAN, true),
              Arguments.of(ScanType.CODE_SCAN, false),

              Arguments.of(ScanType.SECRET_SCAN, true),
              Arguments.of(ScanType.SECRET_SCAN, false),

              Arguments.of(ScanType.IAC_SCAN, true),
              Arguments.of(ScanType.IAC_SCAN, false),

              Arguments.of(ScanType.LICENSE_SCAN, true),
              Arguments.of(ScanType.LICENSE_SCAN, false),

              Arguments.of(ScanType.WEB_SCAN, true),
              Arguments.of(ScanType.WEB_SCAN, false),

              Arguments.of(ScanType.PREPARE, true),
              Arguments.of(ScanType.PREPARE, false));
        }
        /* @formatter:on*/
    }

}
