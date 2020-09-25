// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import java.util.Map;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;

public class EventInspectionAPI {

    public static Map<String, String> fetchMap() {
        IntegrationTestContext context = IntegrationTestContext.get();
        String url = context.getUrlBuilder().buildIntegrationTestFetchEventInspectionStatus();
        String json = context.getSuperAdminRestHelper().getJSon(url);
        @SuppressWarnings("unchecked")
        Map<String, String> map = JSONConverter.get().fromJSON(Map.class, json);
        return map;
    }
    
    public static boolean fetchIsStarted() {
        String value = fetchMap().get("started");
        return Boolean.valueOf(value);
    }
    public static int fetchLastInspectionId() {
        String value = fetchMap().get("lastInspectionId");
        return Integer.valueOf(value);
    }
}
