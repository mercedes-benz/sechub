// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.communication;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CommunicationDataConverterTest {

    static final String WORKING_TEST_CONFIG_JSON = """
            {
                "send" : {
                    "targetType" : "JSON",
                    "mapping" : {
                      "cweId" : "cwe_id",
                      "language" : "lang",
                      "details" : "cwe_details"
                    }
                },

                "receive" : {
                    "sourceType" : "JSON",
                    "mapping" : {
                      "error" : "result_problem",
                      "status" : "result_status_level",
                      "description" : "content"
                    }
                }
            }
            """;
    static final CommunicationDataConverterConfig WORKING_TEST_CONFIG = CommunicationDataConverterConfig.fromJSONString(WORKING_TEST_CONFIG_JSON);

    @Test
    void sendNullMap_returns_empty_string() {
        /* prepare */
        CommunicationDataConverter converterToTest = new CommunicationDataConverter(WORKING_TEST_CONFIG);

        /* execute */
        assertThat(converterToTest.convertForSending(null).isEmpty());
    }

    @Test
    void receiveNullString_returns_just_an_empty_map() {
        /* prepare */
        CommunicationDataConverter converterToTest = new CommunicationDataConverter(WORKING_TEST_CONFIG);

        /* execute */
        assertThat(converterToTest.convertFromReceived(null)).isEmpty();
    }

    @Test
    void send_null_target_type_fails() {
        /* prepare */
        var json = """
                {
                    "send" : {
                        "mapping" : {
                          "cweId" : "cwe_id",
                          "language" : "lang",
                          "details" : "cwe_details"
                        }
                    },

                    "receive" : {
                        "sourceType" : "JSON",
                        "mapping" : {
                          "error" : "result_problem",
                          "status" : "result_status_level",
                          "description" : "content"
                        }
                    }
                }
                """;
        var data = CommunicationDataConverterConfig.fromJSONString(json);

        assertThatThrownBy(() -> new CommunicationDataConverter(data)).isInstanceOf(IllegalArgumentException.class).hasMessageContainingAll("target", "type",
                "not supported", "null");
    }

    @Test
    void receive_null_source_type_fails() {
        /* prepare */
        var json = """
                {
                    "send" : {
                       "targetType" : "JSON",
                        "mapping" : {
                          "cweId" : "cwe_id",
                          "language" : "lang",
                          "details" : "cwe_details"
                        }
                    },

                    "receive" : {
                        "mapping" : {
                          "error" : "result_problem",
                          "status" : "result_status_level",
                          "description" : "content"
                        }
                    }
                }
                """;
        var data = CommunicationDataConverterConfig.fromJSONString(json);

        assertThatThrownBy(() -> new CommunicationDataConverter(data)).isInstanceOf(IllegalArgumentException.class).hasMessageContainingAll("source", "type",
                "not supported", "null");

    }

    @Test
    void send_and_receive_json_with_simple_keys_works() {
        /* prepare */

        CommunicationDataConverter converterToTest = new CommunicationDataConverter(WORKING_TEST_CONFIG);

        var requestDataMap = new HashMap<String, String>();
        requestDataMap.put("cweId", "79");
        requestDataMap.put("language", "java");
        requestDataMap.put("details", "some technical details about the cwe problem");

        /* execute 1 */
        String jsonToSend = converterToTest.convertForSending(requestDataMap);

        /* test 1 */
        var expectedJsonToSend = """
                {
                    "cwe_details" : "some technical details about the cwe problem",
                    "cwe_id" : "79",
                    "lang" : "java"
                }
                """;
        assertThat(jsonToSend).isEqualToIgnoringWhitespace(expectedJsonToSend);

        /* prepare */
        String received = """
                {
                    "error" : "Some error",
                    "status" : "level1",
                    "description" : "Some description"
                }
                """;
        /* execute 1 */
        Map<String, String> receivedMap = converterToTest.convertFromReceived(received);

        /* test 1 */
        assertThat(receivedMap).containsEntry("result_problem", "Some error").containsEntry("result_status_level", "level1").containsEntry("content",
                "Some description");
    }

    @Test
    void sent_json_with_simple_keys_unknown_sent_keys_are_ignored() {
        /* prepare */
        CommunicationDataConverter converterToTest = new CommunicationDataConverter(WORKING_TEST_CONFIG);

        var requestDataMap = new HashMap<String, String>();
        requestDataMap.put("cweId", "79");
        requestDataMap.put("language", "java");
        requestDataMap.put("details", "some technical details about the cwe problem");
        requestDataMap.put("unknown", "i-am-not-known-by-config");

        /* execute 1 */
        String jsonToSend = converterToTest.convertForSending(requestDataMap);

        /* test 1 */
        var expectedJsonToSend = """
                {
                    "cwe_details" : "some technical details about the cwe problem",
                    "cwe_id" : "79",
                    "lang" : "java"
                }
                """;
        assertThat(jsonToSend).isEqualToIgnoringWhitespace(expectedJsonToSend);
    }

    @Test
    void received_json_with_simple_keys_unknown_received_keys_are_ignored() {
        /* prepare */
        CommunicationDataConverter converterToTest = new CommunicationDataConverter(WORKING_TEST_CONFIG);

        String received = """
                {
                    "error" : "Some error",
                    "status" : "level1",
                    "description" : "Some description",
                    "unknown" : "i-am-unknown"
                }
                """;
        /* execute 1 */
        Map<String, String> receivedMap = converterToTest.convertFromReceived(received);

        /* test 1 */
        assertThat(receivedMap).containsEntry("result_problem", "Some error").containsEntry("result_status_level", "level1")
                .containsEntry("content", "Some description").doesNotContainKey("unknown");
    }

}
