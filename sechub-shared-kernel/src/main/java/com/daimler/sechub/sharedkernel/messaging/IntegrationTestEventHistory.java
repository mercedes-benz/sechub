package com.daimler.sechub.sharedkernel.messaging;

import java.util.Map;
import java.util.TreeMap;

import com.daimler.sechub.sharedkernel.util.JSONable;

public class IntegrationTestEventHistory implements JSONable<IntegrationTestEventHistory> {
    
    private static final IntegrationTestEventHistory IMPORT= new IntegrationTestEventHistory();
    
    private Map<Integer, IntegrationTestEventHistoryInspection> idToInspectionMap = new TreeMap<>();
    
    public Map<Integer, IntegrationTestEventHistoryInspection> getIdToInspectionMap() {
        return idToInspectionMap;
    }

    @Override
    public Class<IntegrationTestEventHistory> getJSONTargetClass() {
        return IntegrationTestEventHistory.class;
    }

    public IntegrationTestEventHistoryInspection ensureInspection(int inspectId) {
        return idToInspectionMap.computeIfAbsent(inspectId, key -> createInspection(inspectId));
    }

    private IntegrationTestEventHistoryInspection createInspection(int inspectId) {
        return new IntegrationTestEventHistoryInspection();
    }

    public static IntegrationTestEventHistory fromJSONString(String json) {
        return IMPORT.fromJSON(json);
    }
}
