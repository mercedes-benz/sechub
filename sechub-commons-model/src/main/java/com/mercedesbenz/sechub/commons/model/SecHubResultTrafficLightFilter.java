package com.mercedesbenz.sechub.commons.model;

import java.util.List;

public interface SecHubResultTrafficLightFilter {
    List<SecHubFinding> filterFindingsFor(SecHubResult result, TrafficLight red);

}
