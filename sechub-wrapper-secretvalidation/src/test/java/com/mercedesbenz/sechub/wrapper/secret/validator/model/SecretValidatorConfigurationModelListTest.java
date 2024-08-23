// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;

class SecretValidatorConfigurationModelListTest {

    @Test
    void json_converter_can_handle_model_in_expected_way() {
        /* prepare */
        String json = """
                {
                  "validatorConfigList" : [ {
                    "ruleId" : "rule-id",
                    "categorization" : {
                      "defaultSeverity" : "high",
                      "validationFailedSeverity" : "medium",
                      "validationSuccessSeverity" : "critical"
                    },
                    "requests" : [ {
                      "url" : "https://api.example.com",
                      "proxyRequired" : true,
                      "verifyCertificate" : false,
                      "headers" : [ {
                        "name" : "Authorization",
                        "valuePrefix" : "Bearer"
                      } ],
                      "expectedResponse" : {
                        "httpStatus" : 200,
                        "contains" : {
                          "allOf" : [ "is", "there" ],
                          "oneOf" : [ "success" ]
                        }
                      }
                    } ]
                  } ]
                }
                """;

        String expected = "{\"validatorConfigList\":[{\"ruleId\":\"rule-id\",\"categorization\":{\"defaultSeverity\":\"high\",\"validationFailedSeverity\":\"medium\",\"validationSuccessSeverity\":\"critical\"},"
                + "\"requests\":[{\"url\":\"https://api.example.com\",\"proxyRequired\":true,\"verifyCertificate\":false,\"headers\":[{\"name\":\"Authorization\",\"valuePrefix\":\"Bearer\"}],"
                + "\"expectedResponse\":{\"httpStatus\":200,\"contains\":{\"allOf\":[\"is\",\"there\"],\"oneOf\":[\"success\"]}}}]}]}";

        /* execute */
        SecretValidatorConfigurationModelList fromJson = JSONConverter.get().fromJSON(SecretValidatorConfigurationModelList.class, json);
        String toJson = JSONConverter.get().toJSON(fromJson);

        /* test */
        assertEquals(expected, toJson);
    }

}
