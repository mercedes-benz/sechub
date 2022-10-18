// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.pds.usecase.PDSUseCaseIdentifier;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

class PDSAndSecHubUsecaseTest {

    @Test
    void ensure_unique_ids_of_pds_and_sechub_usescases_are_never_same() {
        List<String> inspectedUniqueIds = new ArrayList<>();

        for (PDSUseCaseIdentifier pdsIdentifier : PDSUseCaseIdentifier.values()) {
            String uniqueId = pdsIdentifier.uniqueId();
            if (inspectedUniqueIds.contains(uniqueId)) {
                fail("PDS usecase identifier:" + pdsIdentifier.name() + " with uniqueId:" + pdsIdentifier.uniqueId()
                        + " is  already found inside inspected unique ids!");
            }
            inspectedUniqueIds.add(uniqueId);
        }

        for (UseCaseIdentifier sechubIdentifier : UseCaseIdentifier.values()) {
            String uniqueId = sechubIdentifier.uniqueId();
            if (inspectedUniqueIds.contains(uniqueId)) {
                fail("SecHub usecase identifier:" + sechubIdentifier.name() + " with uniqueId:" + sechubIdentifier.uniqueId()
                        + " is already found inside inspected unique ids!");
            }
            inspectedUniqueIds.add(uniqueId);
        }
    }

}
