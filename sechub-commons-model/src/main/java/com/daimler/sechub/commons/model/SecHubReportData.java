package com.daimler.sechub.commons.model;

import java.util.Set;
import java.util.UUID;

public interface SecHubReportData {

    Set<SecHubMessage> getMessages();

    SecHubStatus getStatus();

    TrafficLight getTrafficLight();

    SecHubResult getResult();

    UUID getJobUUID();

}