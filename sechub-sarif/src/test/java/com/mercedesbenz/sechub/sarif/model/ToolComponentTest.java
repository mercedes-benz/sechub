// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import static com.mercedesbenz.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.test.TestFileReader;

class ToolComponentTest {

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setGuid("25361018-c7c6-11ec-9fb2-f3f888797467")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setName("other-name")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setFullName("other-full-name")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setProduct("other-product")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setProductSuite("other-product-suite")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setSemanticVersion("2.0.1")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setVersion("2.1.1")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setReleaseDateUtc("2019-02-04T12:08:25.943Z")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setDownloadUri("https://www.otherUri.com/download")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setInformationUri("https://www.otherUri.com/documentation")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setOrganization("other-organization")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setShortDescription(new Message("other"))));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setFullDescription(new Message("other"))));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setLanguage("de")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setComprehensive(true)));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setMinimumRequiredLocalizedDataSemanticVersion("2.0")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setProperties(null)));
        /* @formatter:on */

    }

    @Test
    void tool_component_json_serialization_works_correctly() throws IOException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        File referenceFile = new File("src/test/resources/examples/sarifpropertysnippets/toolcomponent.sarif.json");

        /* execute */
        String jsonReferenceFromFile = TestFileReader.loadTextFile(referenceFile);
        assertNotNull(jsonReferenceFromFile);

        ToolComponent toolComponent = mapper.reader().readValue(jsonReferenceFromFile, ToolComponent.class);
        String jsonCreatedFromObject = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toolComponent);
        assertNotNull(jsonCreatedFromObject);

        /* test */
        assertEquals(jsonReferenceFromFile, jsonCreatedFromObject);
    }

    private ToolComponent createExample() {
        ToolComponent toolComponent = new ToolComponent();

        toolComponent.setGuid("d84e9e96-c7c5-11ec-be2f-9ff76f29cb3b");
        toolComponent.setName("name");
        toolComponent.setFullName("full-name");
        toolComponent.setProduct("product");
        toolComponent.setProductSuite("product-suite");
        toolComponent.setSemanticVersion("1.1.2-beta.12");
        toolComponent.setVersion("1.1.2");
        toolComponent.setReleaseDateUtc("2016-02-08T16:08:25.943Z");
        toolComponent.setDownloadUri("https://www.myUri.com/download/tool-1.1.2-beta.12.zip");
        toolComponent.setInformationUri("https://www.myUri.com/documentation.html");
        toolComponent.setOrganization("organization");
        toolComponent.setShortDescription(new Message());
        toolComponent.setFullDescription(new Message());
        toolComponent.setLanguage("en");
        toolComponent.setRules(new LinkedList<Rule>());
        toolComponent.setTaxa(new LinkedList<Taxon>());
        toolComponent.setComprehensive(false);
        toolComponent.setMinimumRequiredLocalizedDataSemanticVersion("1.1");
        toolComponent.setProperties(new PropertyBag());

        return toolComponent;
    }

}
