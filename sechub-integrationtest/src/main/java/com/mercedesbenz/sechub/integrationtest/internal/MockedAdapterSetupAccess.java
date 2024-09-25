// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetup;
import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetupCombination;
import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetupEntry;
import com.mercedesbenz.sechub.test.TestFileReader;

public class MockedAdapterSetupAccess {
    private static MockedAdapterSetupAccess INSTANCE = new MockedAdapterSetupAccess();
    private MockedAdapterSetup setup;
    private Map<String, MockedAdapterSetupCombination> mapIdToMockCombination = new HashMap<String, MockedAdapterSetupCombination>();

    private MockedAdapterSetupAccess() {
        String json = TestFileReader.readTextFromFile(new File("../sechub-other/mockdata/mockdata_setup.json"));
        setup = TestJSONHelper.get().createFromJSON(json, MockedAdapterSetup.class);

        List<MockedAdapterSetupEntry> entries = setup.getEntries();
        for (MockedAdapterSetupEntry entry : entries) {
            List<MockedAdapterSetupCombination> combinations = entry.getCombinations();
            for (MockedAdapterSetupCombination combination : combinations) {
                String id = combination.getId();
                mapIdToMockCombination.put(id, combination);
            }
        }
    }

    public MockedAdapterSetup getSetup() {
        return setup;
    }

    public MockedAdapterSetupCombination getSetupCombinationById(String id) {
        return mapIdToMockCombination.get(id);
    }

    public static MockedAdapterSetupAccess get() {
        return INSTANCE;
    }

    public Set<String> getCombinationIds() {
        return mapIdToMockCombination.keySet();
    }
}
