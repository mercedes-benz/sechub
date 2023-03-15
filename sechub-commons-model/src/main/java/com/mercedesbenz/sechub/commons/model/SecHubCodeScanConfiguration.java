// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a sechub code scan configuration. It contains a reference set of
 * names of used data configuration objects. As a shortcut (and being downward
 * compatible) the class also extends {@link AbstractSecHubFileSystemContainer}
 * so users can define filesystem configuration also directly without an
 * explicit referenced data section.
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubCodeScanConfiguration extends AbstractSecHubFileSystemContainer implements SecHubDataConfigurationUsageByName {

    private Set<String> namesOfUsedDataConfigurationObjects = new LinkedHashSet<>();

    @Override
    public Set<String> getNamesOfUsedDataConfigurationObjects() {
        return namesOfUsedDataConfigurationObjects;
    }

}
