// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.template;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariableValidation;
import com.mercedesbenz.sechub.test.TestFileReader;

class TemplateDefinitionTest {

    @Test
    public void json_to_from_works() throws Exception {

        /* prepare */

        TemplateDefinition definition = TemplateDefinition.builder().templateId("identifier").templateType(TemplateType.WEBSCAN_LOGIN).assetId("asset1")
                .build();

        TemplateVariable variable1 = new TemplateVariable();
        variable1.setName("variable1");
        variable1.setOptional(true);

        TemplateVariableValidation validation = new TemplateVariableValidation();
        validation.setMinLength(1);
        validation.setMaxLength(100);
        validation.setRegularExpression("[a-z].*");
        variable1.setValidation(validation);
        definition.getVariables().add(variable1);

        /* execute */
        String json = definition.toFormattedJSON();

        /* test */
        TemplateDefinition deserialized = TemplateDefinition.from(json);
        assertThat(deserialized.getId()).isEqualTo("identifier");
        assertThat(deserialized.getType()).isEqualTo(TemplateType.WEBSCAN_LOGIN);
        assertThat(deserialized.getAssetId()).isEqualTo("asset1");
        assertThatList(deserialized.getVariables()).hasSize(1);

        TemplateVariable var1 = deserialized.getVariables().iterator().next();
        assertThat(var1).describedAs("variable1").hasNoNullFieldsOrProperties();
        assertThat(var1.getName()).isEqualTo("variable1");
        assertThat(var1.isOptional()).isTrue();
        assertThat(var1.getValidation()).isNotNull();

        TemplateVariableValidation var1Validation = var1.getValidation();
        assertThat(var1Validation).isNotNull();
        assertThat(var1Validation.getMinLength()).isEqualTo(1);
        assertThat(var1Validation.getMaxLength()).isEqualTo(100);
        assertThat(var1Validation.getRegularExpression()).isEqualTo("[a-z].*");
    }

    @Test
    public void example1_with_type_defined_by_id_and_not_enum_name_can_be_loaded() throws Exception {
        /* prepare */
        String json = TestFileReader.readTextFromFile("./src/test/resources/template/template-definition-example1.json");

        /* execute */
        TemplateDefinition deserialized = TemplateDefinition.from(json);

        /* test */
        assertThat(deserialized.getId()).isEqualTo("identifier");
        assertThat(deserialized.getType()).isEqualTo(TemplateType.WEBSCAN_LOGIN);
        assertThat(deserialized.getAssetId()).isEqualTo("asset0815");
        assertThatList(deserialized.getVariables()).hasSize(1);

        TemplateVariable var1 = deserialized.getVariables().iterator().next();
        assertThat(var1).describedAs("variable1").hasNoNullFieldsOrProperties();
        assertThat(var1.getName()).isEqualTo("variable1");
        assertThat(var1.isOptional()).isTrue();
        assertThat(var1.getValidation()).isNotNull();

        TemplateVariableValidation var1Validation = var1.getValidation();
        assertThat(var1Validation).isNotNull();
        assertThat(var1Validation.getMinLength()).isEqualTo(1);
        assertThat(var1Validation.getMaxLength()).isEqualTo(100);
        assertThat(var1Validation.getRegularExpression()).isEqualTo("[a-z].*");

    }

}
