// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupResult.AutoCleanupResultKey;
import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;

/**
 * This inspector implementation is used for integration tests
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile(PDSProfiles.INTEGRATIONTEST)
public class IntegrationTestPDSAutoCleanupResultInspector implements PDSAutoCleanupResultInspector {

    private Map<AutoCleanupResultKey, Integer> autoCleanupDeletionCountMap = new HashMap<>();

    public void reset() {
        autoCleanupDeletionCountMap.clear();
    }

    @Override
    public void inspect(PDSAutoCleanupResult data) {
        Integer value = autoCleanupDeletionCountMap.computeIfPresent(data.getKey(), (x, y) -> y);
        if (value == null) {
            value = Integer.valueOf(0);
        }
        int deletedAll = data.getDeletedEntries() + value.intValue();
        autoCleanupDeletionCountMap.put(data.getKey(), deletedAll);
    }

    public List<JsonDeleteCount> createList() {
        List<JsonDeleteCount> list = new ArrayList<>();

        for (AutoCleanupResultKey key : autoCleanupDeletionCountMap.keySet()) {
            Integer deleteCount = autoCleanupDeletionCountMap.get(key);
            if (deleteCount == null) {
                deleteCount = -1;
            }
            JsonDeleteCount count = new JsonDeleteCount();
            count.className = key.getInspectedClass().getName();
            count.variant = key.getVariant();
            count.deleteCount = deleteCount;
            list.add(count);
        }
        return list;
    }

    public class JsonDeleteCount {
        public String variant;
        public String className;
        public int deleteCount;
    }

}