// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.JSONConverter;

/**
 * A simple test representation of sechub job status. The origin format from
 * sechub is like this:
 *
 * <code>
 * {"jobUUID":"ae870408-b75b-42b0-a504-bb6729b7aaeb","owner":"scenario5_user1","created":"2022-02-03T08:42:20.317","started":"2022-02-03T08:42:20.574","ended":"2022-02-03T08:42:21.493","state":"ENDED","result":"FAILED","trafficLight":""}
 * </code>
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSecHubJobStatus {

    public static TestSecHubJobStatus fromJSON(String json) {
        return JSONConverter.get().fromJSON(TestSecHubJobStatus.class, json);
    }

    String jobUUID;

    String state;

    String result;

    String trafficLight;

    public String getJobUUID() {
        return jobUUID;
    }

    public String getState() {
        return state;
    }

    public String getResult() {
        return result;
    }

    public String getTrafficLight() {
        return trafficLight;
    }

    public boolean hasResultFailed() {
        return result != null && result.equalsIgnoreCase("failed");
    }

    public boolean hasResultOK() {
        return result != null && result.equalsIgnoreCase("ok");
    }
}
