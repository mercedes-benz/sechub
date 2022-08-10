// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.mapping;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamePatternIdProvider {

    private static final Logger LOG = LoggerFactory.getLogger(NamePatternIdProvider.class);

    private List<NamePatternToIdEntry> entries = new ArrayList<>();

    private String providerId;

    public NamePatternIdProvider(String providerId) {
        this.providerId = providerId;
    }

    public void add(NamePatternToIdEntry entry) {
        if (entry == null) {
            LOG.warn("Ignoring null entry");
            return;
        }
        LOG.debug("'{}' added {}", getProviderId(), entry);
        entries.add(entry);
    }

    /**
     * Resolves id for given name or <code>null</code> when no matchers available
     *
     * @param name
     * @return id or <code>null</code>
     */
    public String getIdForName(String name) {
        String id = null;
        for (NamePatternToIdEntry entry : entries) {
            if (entry.isMatching(name)) {
                id = entry.getId();
                break;
            }
        }
        LOG.debug("'{}' returns id:{} for name:{}", getProviderId(), id, name);
        return id;
    }

    public String getProviderId() {
        return providerId;
    }

    @Override
    public String toString() {
        return "NamePatternIdProvider [providerId=" + providerId + ", entries=" + entries + "]";
    }

}
