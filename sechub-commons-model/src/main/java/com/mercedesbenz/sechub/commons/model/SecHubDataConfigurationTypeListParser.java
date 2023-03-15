// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;

public class SecHubDataConfigurationTypeListParser {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubDataConfigurationTypeListParser.class);

    /**
     * Parses a given string and returns a result set or <code>null</code>.
     *
     * @param commaSeparatedList
     * @return set or <code>null</code> if the comma separated list did contain
     *         wrong values or was empty
     */
    public Set<SecHubDataConfigurationType> fetchTypesAsSetOrNull(String commaSeparatedList) {
        Set<SecHubDataConfigurationType> set = new LinkedHashSet<>();

        List<String> list = SimpleStringUtils.createListForCommaSeparatedValues(commaSeparatedList);
        for (String entry : list) {
            boolean found = false;
            for (SecHubDataConfigurationType type : SecHubDataConfigurationType.values()) {
                if (type.name().equalsIgnoreCase(entry)) {
                    found = true;
                    set.add(type);
                    break;
                }
            }

            if (!found) {
                LOG.debug("Found invalid data type entry:{}.", entry);
                return null;
            }
        }
        if (set.size() == 0) {
            LOG.debug("No data types found.");
            return null;
        }

        return set;
    }
}
