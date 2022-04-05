// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.HashMap;
import java.util.Map;

public abstract class DomainMessagePart {

    private MessageID id;
    protected Map<String, String> parameters;

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
        String data = parameters.get(key.getId());
        return key.getProvider().get(data);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + ", parameters=" + parameters + "]";
    }

    public <T> void set(MessageDataKey<T> key, T content) {
        if (key == null) {
            throw new IllegalArgumentException("key may not be null!");
        }
        String contentAsString = key.getProvider().getString(content);
        parameters.put(key.getId(), contentAsString);
    }

    String getRaw(String key) {
        return parameters.get(key);
    }

    private <T> void assertKeyNotNull(MessageDataKey<T> key) {
        if (key == null) {
            throw new IllegalArgumentException("key may not be null!");
        }
    }

}
