// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;

public class ScheduleEncryptionPool {

    Map<Long, PersistentCipher> poolDataIdToPersistentCipherMap = new LinkedHashMap<>();

    ScheduleEncryptionPool(Map<Long, PersistentCipher> map) {
        if (map != null) {
            poolDataIdToPersistentCipherMap.putAll(map);
        }
    }

    /**
     * Resolves cipher for given pool id
     *
     * @param poolId
     * @return cipher instance or <code>null</code> if not found inside pool
     */
    public PersistentCipher getCipherForPoolId(Long poolId) {
        return poolDataIdToPersistentCipherMap.get(poolId);
    }

    public Set<Long> getAllPoolIds() {
        return new HashSet<>(poolDataIdToPersistentCipherMap.keySet());
    }
}