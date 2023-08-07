// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.pds.usecase.PDSUseCaseIdentifier;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

class PDSAndSecHubUsecaseTest {

    @Test
    void ensure_unique_ids_of_pds_and_sechub_usescases_are_never_same() {
        Map<String, Object> inspectedUniqueIds = new HashMap<>();

        for (PDSUseCaseIdentifier pdsIdentifier : PDSUseCaseIdentifier.values()) {
            String uniqueId = pdsIdentifier.uniqueId();
            if (inspectedUniqueIds.keySet().contains(uniqueId)) {
                fail("PDS usecase identifier:" + pdsIdentifier.name() + " with uniqueId:" + pdsIdentifier.uniqueId()
                        + " is  already found inside inspected unique ids! Existing identifier object is:" + showEnumReadable(inspectedUniqueIds, uniqueId));
            }
            inspectedUniqueIds.put(uniqueId, pdsIdentifier);
        }

        for (UseCaseIdentifier sechubIdentifier : UseCaseIdentifier.values()) {
            String uniqueId = sechubIdentifier.uniqueId();
            if (inspectedUniqueIds.keySet().contains(uniqueId)) {
                fail("SecHub usecase identifier:" + sechubIdentifier.name() + " with uniqueId:" + sechubIdentifier.uniqueId()
                        + " is already found inside inspected unique ids! Existing identifier object is:" + showEnumReadable(inspectedUniqueIds, uniqueId));
            }
            inspectedUniqueIds.put(uniqueId, sechubIdentifier);
        }
    }

    private String showEnumReadable(Map<String, Object> inspectedUniqueIds, String uniqueId) {
        Object obj = inspectedUniqueIds.get(uniqueId);
        if (obj == null) {
            return "null";
        }

        return obj.getClass().getSimpleName() + "." + obj.toString();
    }

}
