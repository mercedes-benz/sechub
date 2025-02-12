// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.template;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Template data for SecHub configuration model. Here users can define user
 * specific template data - e.g. variables like "username", "password"
 *
 * @author Albert Tregnaghi
 *
 */
public class TemplateData {

    private SortedMap<String, String> variables = new TreeMap<>();

    /**
     * Return a sorted map containing variable names as keys and variable values as
     * values.
     *
     * @return sorted variable map
     */
    public SortedMap<String, String> getVariables() {
        return variables;
    }

}
