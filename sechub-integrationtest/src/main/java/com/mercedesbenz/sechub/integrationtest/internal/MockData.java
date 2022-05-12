// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetupCombination;

/**
 * The enumeration represents a programming interface to "mockdata_setup.json"
 * Every name of an enumaration is mapped exactly to the id inside combinations
 * of "mockdata_setup.json"!
 *
 * @author Albert Tregnaghi
 *
 */
public enum MockData {
    /* Checkmarx */
    CHECKMARX_GREEN_5_SECONDS_WAITING,

    CHECKMARX_MULTI_ZERO_WAIT,

    CHECKMARX_MULTI_2_SECONDS_WAITING,

    CHECKMARX_GREEN_10_MILLIS_WAITING,

    CHECKMARX_GREEN_4_SECONDS_WAITING,

    CHECKMARX_GREEN_ZERO_WAIT,

    CHECKMARX_GREEN_1_SECOND_WAITING,

    /* Netsparker */
    CHECKMARX_GREEN_ZERO_WAIT_FALLBACK,

    NETSPARKER_GREEN_ZERO_WAIT,

    NETSPARKER_GREEN_10_SECONDS_WAITING,

    NETSPARKER_RED_ZERO_WAIT,

    NETSPARKER_MULTI_ZERO_WAIT,

    NETSPARKER_GREEN_ZERO_WAIT_FALLBACK,

    NESSUS_GREEN_ZERO_WAIT,

    NESSUS_MULTI_ZERO_WAIT,

    ;

    private String id;

    private MockData() {
        this.id = name().toLowerCase();
    }

    public String getId() {
        return id;
    }

    public boolean isTargetUsedAsFolder() {
        return getCombination().isTargetUsedAsFolder();
    }

    public boolean isTargetNeedingExistingData() {
        return getCombination().isTargetNeedsExistingData();
    }

    public String getTarget() {
        return getCombination().getTarget();
    }

    public String getMockResultFilePath() {
        return getCombination().getFilePath();
    }

    MockedAdapterSetupCombination getCombination() {
        MockedAdapterSetupCombination combination = MockedAdapterSetupAccess.get().getSetupCombinationById(id);
        if (combination == null) {
            throw new IllegalStateException(
                    "Did not found mock combinanation for id:" + id + "\ninside:\n" + MockedAdapterSetupAccess.get().getCombinationIds());
        }
        return combination;
    }
}
