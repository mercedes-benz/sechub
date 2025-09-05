// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.pds;

import com.mercedesbenz.sechub.docgen.Generator;
import com.mercedesbenz.sechub.wrapper.infralight.cli.InfralightWrapperKeys;

public class CheckmarxWrapperDocumentationGenerator implements Generator {

    private PDSSolutionVariableContentGenerator variableContentGenerator = new PDSSolutionVariableContentGenerator();

    public String generateEnvironmentAndJobParameterTable() {
        return variableContentGenerator.generateEnvironmentAndJobParameterTable(InfralightWrapperKeys.values());
    }

    public static void main(String[] args) {
        System.out.println(new CheckmarxWrapperDocumentationGenerator().generateEnvironmentAndJobParameterTable());
    }

}
