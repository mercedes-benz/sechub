// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.docgen.util.DocGenTextFileReader;

class RestDocResourceModelTest {

    @Test
    void test() {
        /* prepare */
        File file = new File("./src/test/resources/restdoc/test_resource_1.json");
        DocGenTextFileReader reader = new DocGenTextFileReader();
        String json = reader.readTextFromFile(file);

        /* execute */
        RestDocResourceModel model = RestDocResourceModel.fromString(json);

        /* test */
        assertNotNull(model);
        assertNotNull(model.request);
        assertEquals("DELETE", model.request.method);
        assertEquals("/api/admin/user/{userId}", model.request.path);

        assertNotNull(model.response);
        assertEquals(200, model.response.status);

    }

}
