// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.usecase;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PDSUseCaseIdentifierTest {

    @Test
    void usecase_numbers_are_unique() {
        for (PDSUseCaseIdentifier identifier : PDSUseCaseIdentifier.values()) {
            searchForDuplicates(identifier);
        }
    }

    @Test
    void usecase_numbers_are_without_fragmentation() {
        List<String> list = new ArrayList<>();
        for (PDSUseCaseIdentifier identifier : PDSUseCaseIdentifier.values()) {
            list.add(identifier.uniqueId());
        }
        for (int i = 0; i < list.size(); i++) {
            int nr = i + 1;
            String toSearch = PDSUseCaseIdentifier.createUseCaseID(nr);
            if (!list.contains(toSearch)) {
                fail("Expected usecase " + toSearch + " was not found! Seems to be there is a fragmentation!");
            }

        }
    }

    private void searchForDuplicates(PDSUseCaseIdentifier source) {
        for (PDSUseCaseIdentifier identifier : PDSUseCaseIdentifier.values()) {
            if (source == identifier) {
                continue;
            }
            /* not same so check id */
            if (source.uniqueId().equalsIgnoreCase(identifier.uniqueId())) {
                fail("Found duplicates:\n" + source.name() + " and\n" + identifier.name() + "\n  have both same identifier:" + identifier.uniqueId());
            }
        }
    }

}
