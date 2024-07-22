// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ScheduleLatestCipherPoolIdResolver {

    /**
     * Resolve latest entry from given list of data entries
     *
     * @param entries a list of pool data elements
     * @return latest entry of list, or <code>null</code> of entry list was empty or
     *         null
     */
    public Long resolveLatestPoolId(List<ScheduleCipherPoolData> entries) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }

        ScheduleCipherPoolData latestCipherPoolData = null;
        for (ScheduleCipherPoolData poolData : entries) {

            /* calculate latest */
            if (latestCipherPoolData == null) {
                latestCipherPoolData = poolData;
            } else {
                if (latestCipherPoolData.getCreated().isBefore(poolData.getCreated())) {
                    latestCipherPoolData = poolData;
                }
            }
        }
        if (latestCipherPoolData == null) {
            return null;
        }
        return latestCipherPoolData.getId();
    }

}
