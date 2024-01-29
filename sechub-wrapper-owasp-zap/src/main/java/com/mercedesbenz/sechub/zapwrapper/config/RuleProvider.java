// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.zapwrapper.config.data.ZapFullRuleset;

public class RuleProvider {

    private TextFileReader reader = new TextFileReader();

    /**
     *
     * @param fullRulesetFile
     * @return ZapFullRuleset specified by file or new empty ZapFullRuleset if file
     *         is <code>null</code> or does not exist
     */
    public ZapFullRuleset fetchFullRuleset(File fullRulesetFile) {
        ZapFullRuleset fullRuleset = new ZapFullRuleset();
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
            throw new ZapWrapperRuntimeException("Error reading file: " + file, e, ZapWrapperExitCode.IO_ERROR);
        }
    }

}
