// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public abstract class DomainMessagePart {

    private MessageID id;
    protected Map<String, SealedObject> parameters;

    DomainMessagePart(MessageID id) {
        this.id = id;
        this.parameters = new HashMap<>();
    }

    public boolean hasID(MessageID messageId) {
        if (messageId == null) {
            return false;
        }
        return messageId.equals(id);
    }

    public MessageID getMessageId() {
        return id;
    }

    /**
     * Get parameter value by given key
     *
     * @param key
     * @return value or <code>null</code>
     */
    public <T> T get(MessageDataKey<T> key) {
        assertKeyNotNull(key);
        SealedObject sealedObject = parameters.get(key.getId());
        return key.getProvider().get(CryptoAccess.CRYPTO_STRING.unseal(sealedObject));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + ", parameters=" + parameters + "]";
    }

    public <T> void set(MessageDataKey<T> key, T content) {
        if (key == null) {
            throw new IllegalArgumentException("key may not be null!");
        }
        parameters.put(key.getId(), CryptoAccess.CRYPTO_STRING.seal(key.getProvider().getString(content)));
    }

    String getRaw(String key) {
        return CryptoAccess.CRYPTO_STRING.unseal(parameters.get(key));
    }

    private <T> void assertKeyNotNull(MessageDataKey<T> key) {
        if (key == null) {
            throw new IllegalArgumentException("key may not be null!");
        }
    }

}
