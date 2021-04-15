// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.UUID;

/**
 * A reduced view to executor configurations - contains reduced configuration
 * data for list
 * 
 * @author Albert Tregnaghi
 *
 */
public class ProductExecutorConfigListEntry {

    public ProductExecutorConfigListEntry() {

    }

    public ProductExecutorConfigListEntry(UUID uuid, String name, boolean enabled) {
        this.uuid = uuid;
        this.name = name;
        this.enabled = enabled;
    }

    UUID uuid;
    String name;

    Boolean enabled;

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
