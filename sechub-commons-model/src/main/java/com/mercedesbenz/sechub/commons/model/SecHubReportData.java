// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Set;
import java.util.UUID;

public interface SecHubReportData {

    void setMessages(Set<SecHubMessage> messages);

    Set<SecHubMessage> getMessages();

    void setStatus(SecHubStatus status);

    SecHubStatus getStatus();

    void setTrafficLight(TrafficLight trafficLight);

    TrafficLight getTrafficLight();

    void setResult(SecHubResult result);

    SecHubResult getResult();

    void setJobUUID(UUID jobUUID);

    UUID getJobUUID();

    String getReportVersion();

    void setReportVersion(String version);

    public SecHubReportMetaData getMetaData();

    public void setMetaData(SecHubReportMetaData metaData);

}