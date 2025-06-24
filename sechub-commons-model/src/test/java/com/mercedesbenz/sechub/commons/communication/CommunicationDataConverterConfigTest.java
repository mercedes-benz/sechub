// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.communication;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.communication.CommunicationDataConverterConfig.Receive;
import com.mercedesbenz.sechub.commons.communication.CommunicationDataConverterConfig.Send;
import com.mercedesbenz.sechub.test.TestFileReader;

class CommunicationDataConverterConfigTest {

    @Test
    void config_from_file_can_be_created() {
        /* prepar */
        String testJson = TestFileReader.readTextFromFile("./src/test/resources/communciation/communication-key-converter-config1.json");

        /* execute */
        CommunicationDataConverterConfig result = CommunicationDataConverterConfig.fromJSONString(testJson);

        /* test @formatter:off */
        Send send = result.getSend();
        assertThat(send.getTargetType()).isEqualTo(CommunicationDataConversionType.JSON);
        assertThat(send.getMapping()).
            containsEntry("cweId", "cwe.id").
            containsEntry("language", "lang").
            containsEntry("details", "cwe.details");

        Receive receive = result.getReceive();
        assertThat(receive.getSourceType()).isEqualTo(CommunicationDataConversionType.JSON);
        assertThat(receive.getMapping()).
            containsEntry("error", "result.problem").
            containsEntry("status", "result.status.level").
            containsEntry("description", "content");

        /* @formatter:on*/
    }

}
