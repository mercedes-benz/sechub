// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.messaging;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;
import com.mercedesbenz.sechub.test.TestConstants;

class UseCaseEventMessageLinkAsciidocGeneratorManualTest {

    @Test
    @EnabledIfSystemProperty(named = TestConstants.MANUAL_TEST_BY_DEVELOPER, matches = "true", disabledReason = TestConstants.DESCRIPTION_DISABLED_BECAUSE_A_MANUAL_TEST_FOR_GENERATION)
    void manualTestByDeveloper() throws Exception {
        System.setProperty("com.mercedesbenz.sechub.docgen.debug", "true");

        File outputFolder = new File("./build/tmp/gen-asciidoc/");

        Map<UseCaseIdentifier, Set<String>> usecaseToMessageIdMap = new TreeMap<>();
        Set<String> list = new TreeSet<>();
        list.add(MessageID.JOB_DONE.name());
        usecaseToMessageIdMap.put(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB, list);

        list = new TreeSet<>();
        list.add(MessageID.JOB_DONE.name());
        usecaseToMessageIdMap.put(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB_HARD, list);

        UseCaseEventMessageLinkAsciidocGenerator generator = new UseCaseEventMessageLinkAsciidocGenerator(usecaseToMessageIdMap, outputFolder);
        generator.generate();
    }

}
