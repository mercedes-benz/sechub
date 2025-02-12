// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.template;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Template data for SecHub configuration model. Here users can define user
 * specific template data - e.g. variables like "username", "password"
 *
 * @author Albert Tregnaghi
 *
 */
public class TemplateData {

    private Map<String, String> variables = new LinkedHashMap<>(); // entries are ordered in model like ordered in json

    public Map<String, String> getVariables() {
        return variables;
    }

}
