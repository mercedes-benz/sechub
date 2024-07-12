package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;

public class EncryptionPool {

    Map<Long, PersistentCipher> poolDataIdToPersistentCipherMap = new LinkedHashMap<>();

    EncryptionPool(Map<Long, PersistentCipher> map) {
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
}