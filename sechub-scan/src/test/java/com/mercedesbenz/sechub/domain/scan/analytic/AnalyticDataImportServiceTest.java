// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.analytic;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticDataPart;
import com.mercedesbenz.sechub.sharedkernel.analytic.CodeAnalyticData;

class AnalyticDataImportServiceTest {

    private static final String SOURCE_CODE = "pseudoCode";
    private AnalyticDataImportService importServiceToTest;
    private AnalyticData target;
    private List<AnalyticDataPartImporter<?>> importers;

    @BeforeEach
    void beforeEach() {
        importServiceToTest = new AnalyticDataImportService();

        importers = new ArrayList<>(); // simulate injection by spring boot
        importServiceToTest.importers = importers;

        target = mock(AnalyticData.class);

    }

    @Test
    void no_target_changes_when_no_importer_is_available() {

        /* execute */
        importServiceToTest.importAnalyticDataParts(SOURCE_CODE, target);

        /* test */
        verifyNoInteractions(target);
    }

    @Test
    @SuppressWarnings("unchecked")
    void target_code_analytic_data_changes_when_importer_is_able_to_import_and_returns_codeanalytic_data() throws Exception {
        /* prepare */
        AnalyticDataPartImporter<CodeAnalyticData> importer1 = mock(AnalyticDataPartImporter.class);
        CodeAnalyticData data = new CodeAnalyticData();
        when(importer1.importData(SOURCE_CODE)).thenReturn(data);
        when(importer1.isAbleToImport(SOURCE_CODE)).thenReturn(true);

        /* prepare */
        importers.add(importer1);

        /* execute */
        importServiceToTest.importAnalyticDataParts(SOURCE_CODE, target);

        /* test */
        verify(target).setCodeAnalyticData(data);
    }

    @Test
    @SuppressWarnings("unchecked")
    void target_code_analytic_data_does_not_change_when_importer_is_NOT_able_to_import_and_returns_codeanalytic_data() throws Exception {
        /* prepare */
        AnalyticDataPartImporter<CodeAnalyticData> importer1 = mock(AnalyticDataPartImporter.class);
        CodeAnalyticData data = new CodeAnalyticData();
        when(importer1.importData(SOURCE_CODE)).thenReturn(data);
        when(importer1.isAbleToImport(SOURCE_CODE)).thenReturn(false);

        /* prepare */
        importers.add(importer1);

        /* execute */
        importServiceToTest.importAnalyticDataParts(SOURCE_CODE, target);

        /* test */
        verifyNoInteractions(target);
    }

    @Test
    @SuppressWarnings("unchecked")
    void target_code_analytic_data_does_not_change_when_importer_is_able_to_import_but_returns_unsupported_data_part() throws Exception {
        /* prepare */
        AnalyticDataPartImporter<AnalyticDataPart> importer1 = mock(AnalyticDataPartImporter.class);
        AnalyticDataPart data = mock(AnalyticDataPart.class);
        when(importer1.importData(SOURCE_CODE)).thenReturn(data);
        when(importer1.isAbleToImport(SOURCE_CODE)).thenReturn(true);

        /* prepare */
        importers.add(importer1);

        /* execute */
        importServiceToTest.importAnalyticDataParts(SOURCE_CODE, target);

        /* test */
        verifyNoInteractions(target);
    }

}
