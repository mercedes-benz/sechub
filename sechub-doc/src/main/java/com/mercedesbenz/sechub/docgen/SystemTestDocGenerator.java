// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import com.mercedesbenz.sechub.docgen.adopt.AdoptedSystemTestDefaultFallback;
import com.mercedesbenz.sechub.docgen.adopt.AdoptedSystemTestRuntimeVariable;

public class SystemTestDocGenerator {

    public String generateDefaultFallbackTable() {
        /* @formatter:off */
        String data = "[cols=\"2,1,4\", options=\"header\"]\n"
                + "|===\n"
                + "|Type   |Scope   | Default value\n"
                + "//--------------------------------------------------------\n";

        for (AdoptedSystemTestDefaultFallback fallback: AdoptedSystemTestDefaultFallback.values()) {
            data = data+ "|"+fallback.getDescription()+ "|`"+fallback.getScope()+"` | `"+ fallback.getValue()+"` \n";
        }

        data= data
                + "|===\n"
                + "";
        /* @formatter:on */
        return data;

    }

    public String generateRuntimeVariableTable() {
        /* @formatter:off */
        String data = "[cols=\"1,2\",options=\"header\"]\n"
                + "|===\n"
                + "|Variable name | Description\n"
                + "//--------------------------------------------------------\n";

        for (AdoptedSystemTestRuntimeVariable runtimeVariable: AdoptedSystemTestRuntimeVariable.values()) {
            data = data+ "|`"+runtimeVariable.getVariableName()+ "`|"+runtimeVariable.getDescription()+"\n";
        }

        data= data
                + "|===\n"
                + "";
        /* @formatter:on */
        return data;

    }
}
