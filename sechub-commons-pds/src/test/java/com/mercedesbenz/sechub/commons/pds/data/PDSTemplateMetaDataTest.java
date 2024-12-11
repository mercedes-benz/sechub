// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData.PDSAssetData;
import com.mercedesbenz.sechub.test.TestFileReader;

class PDSTemplateMetaDataTest {

    @ParameterizedTest
    @ValueSource(strings = { "pds-param-template-metadata-example1.json", "pds-param-template-metadata-syntax.json" })
    void examples_in_doc_are_valid(String fileName) {
        /* prepare */
        String json = TestFileReader.readTextFromFile("./../sechub-doc/src/docs/asciidoc/documents/shared/snippet/" + fileName);

        /* execute */
        List<PDSTemplateMetaData> result = JSONConverter.get().fromJSONtoListOf(PDSTemplateMetaData.class, json);

        /* test */
        assertEquals(1, result.size());
        PDSTemplateMetaData entry = result.iterator().next();
        assertNotNull(entry.getTemplateId());
        PDSAssetData assetData = entry.getAssetData();
        assertNotNull(assetData);
        assertNotNull(assetData.getAssetId());
        assertNotNull(assetData.getChecksum());
        assertNotNull(assetData.getFileName());
    }

}
