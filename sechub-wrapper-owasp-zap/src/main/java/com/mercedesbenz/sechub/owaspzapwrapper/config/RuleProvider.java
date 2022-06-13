// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.OwaspZapFullRuleset;

public class RuleProvider {

    private TextFileReader reader = new TextFileReader();

    public OwaspZapFullRuleset fetchFullRuleset(File fullRulesetFile) {
        OwaspZapFullRuleset fullRuleset = new OwaspZapFullRuleset();
        if (fullRulesetFile == null) {
            return fullRuleset;
        }
        if (!fullRulesetFile.exists()) {
            return fullRuleset;
        }
        String contentAsJSON = readFileContent(fullRulesetFile);
        return fullRuleset.fromJSON(contentAsJSON);
    }

    public DeactivatedRuleReferences fetchDeactivatedRuleReferences(File rulesDeactvationFile) {
        DeactivatedRuleReferences deactivatedRuleReferences = new DeactivatedRuleReferences();
        if (rulesDeactvationFile == null) {
            return deactivatedRuleReferences;
        }
        if (!rulesDeactvationFile.exists()) {
            return deactivatedRuleReferences;
        }

        String contentAsJSON = readFileContent(rulesDeactvationFile);
        return deactivatedRuleReferences.fromJSON(contentAsJSON);
    }

    private String readFileContent(File file) {
        try {
            return reader.loadTextFile(file);
        } catch (IOException e) {
            throw new MustExitRuntimeException("Error reading file: " + file, e, MustExitCode.RULE_FILE_ERROR);
        }
    }

}
