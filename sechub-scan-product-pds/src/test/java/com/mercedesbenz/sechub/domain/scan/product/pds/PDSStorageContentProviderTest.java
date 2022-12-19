// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    @ValueSource(booleans = { true, false })
    void isBinaryRequired_storage_not_reused_is_handled_by_modelsupport_result(boolean required) {
        /* prepare */
        when(modelSupport.isBinaryRequired(ScanType.CODE_SCAN, model)).thenReturn(required);
        PDSStorageContentProvider providerToTest = new PDSStorageContentProvider(storage, false, ScanType.CODE_SCAN, modelSupport, model);

        /* execute */
        assertEquals(required, providerToTest.isBinaryRequired());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void isSourceRequired_storage_not_reused__is_handled_by_modelsupport_result(boolean required) {
        /* prepare */
        when(modelSupport.isSourceRequired(ScanType.CODE_SCAN, model)).thenReturn(required);
        PDSStorageContentProvider providerToTest = new PDSStorageContentProvider(storage, false, ScanType.CODE_SCAN, modelSupport, model);

        /* execute */
        assertEquals(required, providerToTest.isSourceRequired());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void isBinaryRequired_storage_reused_is_handled_by_modelsupport_result(boolean required) {
        /* prepare */
        when(modelSupport.isBinaryRequired(ScanType.CODE_SCAN, model)).thenReturn(required);
        PDSStorageContentProvider providerToTest = new PDSStorageContentProvider(storage, true, ScanType.CODE_SCAN, modelSupport, model);

        /* execute */
        assertEquals(required, providerToTest.isBinaryRequired());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void isSourceRequired_storage_reused__is_handled_by_modelsupport_result(boolean required) {
        /* prepare */
        when(modelSupport.isSourceRequired(ScanType.CODE_SCAN, model)).thenReturn(required);
        PDSStorageContentProvider providerToTest = new PDSStorageContentProvider(storage, true, ScanType.CODE_SCAN, modelSupport, model);

        /* execute */
        assertEquals(required, providerToTest.isSourceRequired());
    }

}
