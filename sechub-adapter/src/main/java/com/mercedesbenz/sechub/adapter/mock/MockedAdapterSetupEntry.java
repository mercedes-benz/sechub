// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.mock;

import java.util.ArrayList;
import java.util.List;

/**
 * A configuration object for mocked adapters, will be used for marshalling and
 * inside mocked adapters to handle different behaviours
 *
 * @author Albert Tregnaghi
 *
 */
public class MockedAdapterSetupEntry {

    /**
     * full class name of the mock adapter - used as an identifier
     */
    private String adapterId;

    private List<MockedAdapterSetupCombination> combinations = new ArrayList<>();

    public void setAdapterId(String adapterid) {
        this.adapterId = adapterid;
    }

    public String getAdapterId() {
        return adapterId;
    }

    public boolean isThrowingAdapterExceptionFor(String target) {
        MockedAdapterSetupCombination combination = getCombination(target);
        if (combination == null) {
            return false;
        }
        return combination.isThrowsAdapterException();
    }

    public String getResultFilePathFor(String target) {
        MockedAdapterSetupCombination combination = getCombination(target);
        if (combination == null) {
            return null;
        }
        return combination.getFilePath();
    }

    public long getTimeToElapseInMilliseconds(String target) {
        MockedAdapterSetupCombination combination = getCombination(target);
        if (combination == null) {
            return -1;
        }
        return combination.getTimeToElapseInMilliseconds();
    }

    public List<MockedAdapterSetupCombination> getCombinations() {
        return combinations;
    }

    public MockedAdapterSetupCombination getCombination(String mockDataIdentifier) {
        MockedAdapterSetupCombination combi = internalExactGetCombination(mockDataIdentifier);
        if (combi != null) {
            return combi;
        }
        return internalExactGetCombination(MockedAdapterSetupCombination.ANY_OTHER_TARGET);
    }

    private MockedAdapterSetupCombination internalExactGetCombination(String mockDataIdentifier) {
        if (mockDataIdentifier == null) {
            return null;
        }
        for (MockedAdapterSetupCombination combination : combinations) {
            if (combination == null) {
                continue;
            }
            String combinationMockDataIdentifier = combination.getMockDataIdentifier();
            if (combinationMockDataIdentifier == null) {
                continue;
            }
            if (mockDataIdentifier.startsWith(combinationMockDataIdentifier)) {
                return combination;
            }
        }
        return null;
    }

}
