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

    /**
     *
     * @param fullRulesetFile
     * @return OwaspZapFullRuleset specified by file or new empty
     *         OwaspZapFullRuleset if file is <code>null</code> or does not exist
     */
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

    /**
     *
     * @param rulesDeactvationFile
     * @return DeactivatedRuleReferences specified by file or new empty
     *         DeactivatedRuleReferences if file is <code>null</code> or does not
     *         exist
     */
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
