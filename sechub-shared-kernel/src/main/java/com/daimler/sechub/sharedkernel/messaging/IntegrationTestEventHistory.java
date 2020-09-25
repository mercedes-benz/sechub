// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.util.SortedMap;
import java.util.TreeMap;

import com.daimler.sechub.commons.model.JSONable;

public class IntegrationTestEventHistory implements JSONable<IntegrationTestEventHistory> {
    
    private static final IntegrationTestEventHistory IMPORT= new IntegrationTestEventHistory();
    
    private SortedMap<Integer, IntegrationTestEventHistoryInspection> idToInspectionMap = new TreeMap<>();
    
    public SortedMap<Integer, IntegrationTestEventHistoryInspection> getIdToInspectionMap() {
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
