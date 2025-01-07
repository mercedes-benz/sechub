// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.template;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

class TemplateDataResolverTest {

    TemplateDataResolver resolverToTest;

    @BeforeEach
    void beforeEach() {
        resolverToTest = new TemplateDataResolver();
    }

    @Test
    void webscan_login_template_data_can_be_resolved_when_defined_in_model() {

        /* prepare */
        String json = """
                {
                  "webScan" : {
                    "login" : {

                      "templateData" : {
                        "variables" : {
                          "username" : "the-user",
                          "password" : "the-password"
                        }
                      }
                    }
                  }
                }
                """;
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);

        /* execute */
        TemplateData result = resolverToTest.resolveTemplateData(TemplateType.WEBSCAN_LOGIN, model);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getVariables()).containsEntry("username", "the-user").containsEntry("password", "the-password");
    }

    @Test
    void webscan_login_template_data_cannot_be_resolved_when_not_defined_in_model() {

        /* prepare */
        String json = """
                {
                  "webScan" : {

                  }
                }
                """;
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);

        /* execute */
        TemplateData result = resolverToTest.resolveTemplateData(TemplateType.WEBSCAN_LOGIN, model);

        /* test */
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(TemplateType.class)
    void template_data_is_always_null_when_model_is_empty(TemplateType templateType) {

        /* prepare */
        String json = """
                {
                }
                """;
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);

        /* execute */
        TemplateData result = resolverToTest.resolveTemplateData(templateType, model);

        /* test */
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(TemplateType.class)
    void template_data_is_always_null_when_model_is_null(TemplateType templateType) {

        /* prepare */
        SecHubConfigurationModel model = null;

        /* execute */
        TemplateData result = resolverToTest.resolveTemplateData(templateType, model);

        /* test */
        assertThat(result).isNull();
    }

}
