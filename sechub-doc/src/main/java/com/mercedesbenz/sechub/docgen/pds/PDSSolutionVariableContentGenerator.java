// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.pds;

import com.mercedesbenz.sechub.commons.pds.PDSSolutionVariableKey;
import com.mercedesbenz.sechub.commons.pds.PDSSolutionVariableType;
import com.mercedesbenz.sechub.docgen.Generator;

public class PDSSolutionVariableContentGenerator implements Generator {

    public String generateEnvironmentAndJobParameterTable(PDSSolutionVariableKey... variables) {

        StringBuilder sb = new StringBuilder();
        sb.append("[options=\"header\", cols=\"30,~,50\"]\n");
        sb.append("|===\n");
        sb.append("|Job parameter | Type | Description \n");
        sb.append("//----------------------\n");

        for (PDSSolutionVariableKey variable : variables) {
            PDSSolutionVariableType type = variable.getVariableType();
            sb.append("|");
            sb.append(variable.getVariableKey());
            sb.append("|");
            sb.append(type.getDescription());
            sb.append("|");
            sb.append(variable.getVariableDescription());
            sb.append("\n");
        }
        sb.append("|===\n");

        return sb.toString();

    }

}
