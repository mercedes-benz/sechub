// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import java.io.File;
import java.util.List;

import com.mercedesbenz.sechub.commons.pds.ExecutionPDSKey;
import com.mercedesbenz.sechub.commons.pds.PDSKey;
import com.mercedesbenz.sechub.commons.pds.PDSKeyProvider;
import com.mercedesbenz.sechub.domain.scan.product.pds.PDSExecutorConfigSupport;
import com.mercedesbenz.sechub.pds.execution.PDSKeyToEnvConverter;

public class PDSExecutorConfigurationParameterDescriptionGenerator {

    private PDSKeyToEnvConverter keyToEnvConverter = new PDSKeyToEnvConverter();

    public String generatePDSExecutorConfigurationParamters(File targetFile) {
        StringBuilder sb = new StringBuilder();
        addLine(".PDS executor configuration parameters", sb);
        addLine("[%header,cols=3*]", sb);
        addLine("|===", sb);
        addLine("| Key", sb);
        addLine("| Description", sb);
        addLine("| Additional info", sb);

        addLine("", sb);

        List<PDSKeyProvider<? extends PDSKey>> list = PDSExecutorConfigSupport.getUnmodifiableListOfParameterKeyProvidersForPdsExecutorConfiguration();
        for (PDSKeyProvider<? extends PDSKey> provider : list) {
            PDSKey key = provider.getKey();
            addLine("| " + key.getId(), sb);
            addLine("|\n+++\n" + key.getDescription() + "\n+++\n", sb);
            addLine("a| " + describe(provider), sb);
            addLine("", sb);
        }
        sb.append("|===");

        return sb.toString();
    }

    private String describe(PDSKeyProvider<? extends PDSKey> provider) {
        StringBuilder sb = new StringBuilder();
        PDSKey key = provider.getKey();
        if (key != null) {
            if (key instanceof ExecutionPDSKey) {
                ExecutionPDSKey execKey = (ExecutionPDSKey) key;
                if (execKey.isAvailableInsideScript()) {
                    String env = keyToEnvConverter.convertKeyToEnv(key.getId());
                    appendInfo("Available as ENV variable `" + env + "` inside launcher script.", sb);
                }
            }
            if (key.isGenerated()) {
                appendInfo("Generated", true, sb);
            }
            if (key.isMandatory()) {
                appendInfo("Mandatory", true, sb);
            } else {
                appendInfo("Optional", sb);
            }
            if (key.getDefaultValue() != null) {
                appendInfo("Default is \"" + key.getDefaultValue() + '"', sb);
            }
            if (key.isOnlyForTesting()) {
                appendInfo("Only for testing", true, sb);
            }
        }
        return sb.toString();
    }

    private void appendInfo(String text, StringBuilder sb) {
        appendInfo(text, false, sb);
    }

    private void appendInfo(String text, boolean bold, StringBuilder sb) {
        sb.append("  * ");
        if (bold) {
            sb.append("**");
        }
        sb.append(text);
        if (bold) {
            sb.append("**");
        }
        sb.append("\n");
    }

    private void addLine(String text, StringBuilder sb) {
        sb.append(text).append("\n");
    }

}
