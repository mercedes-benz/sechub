// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;

public class AssertMapping extends AbstractAssert {

    private MappingData data;

    AssertMapping(MappingData data) {
        this.data = data;
    }

    public AssertMapping contains(MappingEntry entry) {
        for (MappingEntry entry2 : data.getEntries()) {
            if (entry2.equals(entry)) {
                return this;
            }
        }
        fail("Mapping does not contain entry:" + entry.toJSON() + "\nbut:\n" + data.toJSON());
        throw new IllegalStateException();
    }

    public AssertMapping hasEntries(int size) {
        assertEquals(size, data.getEntries().size());
        return this;
    }

}
