package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.integrationtest.internal.TestJSONHelper;
import com.fasterxml.jackson.databind.JsonNode;

public class AssertJobReport{
    TrafficLight trafficLight;

    public AssertJobReport(String report) {
        JsonNode data = TestJSONHelper.get().readTree(report);
        JsonNode tl = data.get("trafficLight");
        String trafficLightText = tl.asText();
        this.trafficLight = TrafficLight.fromString(trafficLightText);
    }
    
    public AssertJobReport hasTrafficLight(TrafficLight expected) {
        assertEquals(expected, trafficLight);
        return this;
    }
}