// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.sereco.importer.ProductResultImporter;
import com.mercedesbenz.sechub.sereco.importer.SarifV1JSONImporter;
import com.mercedesbenz.sechub.sereco.importer.SensitiveDataMaskingService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.test.TestFileReader;

public class WorkspaceTest {
    private String projectId = "myTestProject";
    private Workspace workspace;

    private ImporterRegistry registry = mock(ImporterRegistry.class);
    private SensitiveDataMaskingService maskingService = mock(SensitiveDataMaskingService.class);

    @BeforeEach
    void beforeEach() {
        // create mocks
        registry = mock(ImporterRegistry.class);
        maskingService = mock(SensitiveDataMaskingService.class);

        workspace = new Workspace(projectId);

        // assign mocks
        workspace.registry = registry;
        workspace.maskingService = maskingService;
    }

    @Test
    void get_id__not_empty() {
        /* execute */
        workspace = new Workspace(projectId);

        /* test */
        assertEquals(projectId, workspace.getId());
    }

    @Test
    void create_report__no_vulnerabilities() {
        /* execute */
        String report = workspace.createReport();

        /* test */
        assertEquals("{}", report);
    }

    @Test
    void do_import__no_importer() throws IOException {
        /* prepare */
        SecHubMessage info = new SecHubMessage(SecHubMessageType.INFO, "info");
        String importId = "id1";
        String importData = "{}";
        String productId = "PDS_SCANCODE";
        List<SecHubMessage> messages = List.of(info);

        /* formatter:off */
        ImportParameter importParameter = ImportParameter.builder().importData(importData).importId(importId).importProductMessages(messages)
                .productId(productId).build();
        /* formatter:on */

        /* execute */
        IOException exception = assertThrows(IOException.class, () -> workspace.doImport(new SecHubConfiguration(), importParameter));

        /* test */
        assertEquals("Import failed, no importer was able to import product result: " + productId, exception.getMessage());
    }

    @Test
    void do_import_does_call_sensitive_data_masking_service() throws IOException {
        /* prepare */
        SecHubMessage info = new SecHubMessage(SecHubMessageType.INFO, "info");
        String importId = "id1";
        String importData = TestFileReader.loadTextFile("src/test/resources/sarif/sarif_2.1.0_owasp_zap.json");
        String productId = "PDS_WEBSCAN";
        List<SecHubMessage> messages = List.of(info);

        /* formatter:off */
        ImportParameter importParameter = ImportParameter.builder().importData(importData).importId(importId).importProductMessages(messages)
                .productId(productId).build();
        /* formatter:on */

        List<ProductResultImporter> importers = new ArrayList<>();
        importers.add(new SarifV1JSONImporter());
        when(registry.getImporters()).thenReturn(importers);

        doCallRealMethod().when(maskingService).maskSensitiveData(any(), any());

        /* execute */
        workspace.doImport(new SecHubConfiguration(), importParameter);

        /* test */
        verify(maskingService, atLeast(1)).maskSensitiveData(any(), any());
    }
}
