// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

public class WorkspaceTest {
    private String projectId = "myTestProject";
    private Workspace workspace;

    @BeforeEach
    void beforeEach() {
        workspace = new Workspace(projectId);

        ImporterRegistry registry = mock(ImporterRegistry.class);
        workspace.registry = registry;
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
        IOException exception = assertThrows(IOException.class, () -> workspace.doImport(importParameter));

        /* test */
        assertEquals("Import failed, no importer was able to import product result: " + productId, exception.getMessage());
    }
}
