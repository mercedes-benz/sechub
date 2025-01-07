// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UseCaseIdentifierTest {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseIdentifierTest.class);

    @Test
    void usecase_numbers_are_unique() {
        UseCaseIdentifier[] values = UseCaseIdentifier.values();
        // log out the number of values - a hint what number is next one (enum source
        // numbering is fragmented and not always easy to read...)
        LOG.info("Check uniqueness of {}  - found {} entries. Next free identifier number should be: {}", UseCaseIdentifier.class.getSimpleName(),
                values.length, values.length + 1);
        for (UseCaseIdentifier identifier : values) {
            searchForDuplicates(identifier);
        }
    }

    @Test
    void usecase_numbers_are_without_fragmentation() {
        List<String> list = new ArrayList<>();
        for (UseCaseIdentifier identifier : UseCaseIdentifier.values()) {
            list.add(identifier.uniqueId());
        }
        for (int i = 0; i < list.size(); i++) {
            int nr = i + 1;
            String toSearch = UseCaseIdentifier.createUseCaseID(nr);
            if (!list.contains(toSearch)) {
                fail("Expected usecase " + toSearch + " was not found! Seems to be there is a fragmentation!");
            }

        }
    }

    private void searchForDuplicates(UseCaseIdentifier source) {
        for (UseCaseIdentifier identifier : UseCaseIdentifier.values()) {
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
