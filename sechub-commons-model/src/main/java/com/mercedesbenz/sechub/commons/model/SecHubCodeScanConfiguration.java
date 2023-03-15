// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashSet;
import java.util.Set;

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
public class SecHubCodeScanConfiguration extends AbstractSecHubFileSystemContainer implements SecHubDataConfigurationUsageByName {

    private Set<String> namesOfUsedDataConfigurationObjects = new LinkedHashSet<>();

    @Override
    public Set<String> getNamesOfUsedDataConfigurationObjects() {
        return namesOfUsedDataConfigurationObjects;
    }

}
