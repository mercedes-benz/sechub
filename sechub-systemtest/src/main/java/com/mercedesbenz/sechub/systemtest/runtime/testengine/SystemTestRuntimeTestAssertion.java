// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mercedesbenz.sechub.api.internal.gen.model.ScanSecHubReport;
import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.AssertContainsStringsDefinition;
import com.mercedesbenz.sechub.systemtest.config.AssertEqualsFileDefinition;
import com.mercedesbenz.sechub.systemtest.config.AssertSechubResultDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestAssertDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.testengine.SystemTestRuntimeTestEngine.TestEngineTestContext;
import com.mercedesbenz.sechub.systemtest.runtime.testengine.TestTemplateSupport.TemplateMatchResult;

public class SystemTestRuntimeTestAssertion {

    public void assertTest(TestAssertDefinition assertDefinition, TestEngineTestContext testContext) {
        handleSecHubAsserts(assertDefinition, testContext);
    }

    private void handleSecHubAsserts(TestAssertDefinition assertDefinition, TestEngineTestContext testContext) {
        if (testContext.hasFailed()) {
            // already marked as failed - e.g. by other test asserts
            return;
        }

        if (!testContext.isSecHubTest()) {
            return;
        }

        Optional<AssertSechubResultDefinition> sechubResult = assertDefinition.getSechubResult();
        if (sechubResult.isEmpty()) {
            return;
        }

        handleSecHubAssert(sechubResult.get(), testContext);
    }

    private void handleSecHubAssert(AssertSechubResultDefinition sechubResultAssert, TestEngineTestContext testContext) {
        ScanSecHubReport report = testContext.getSecHubRunData().getReport();
        if (report == null) {
            testContext.markAsFailed("Sechub report not available",
                    "This should not happen. Please look at the unit test console log and inside SecHub server log for details");
            return;
        }
        String reportAsJson = JSONConverter.get().toJSON(report, true);

        if (sechubResultAssert.getHasTrafficLight().isPresent()) {
            TrafficLight expected = sechubResultAssert.getHasTrafficLight().get();
            TrafficLight actual = TrafficLight.fromValue(report.getTrafficLight());
            if (!expected.equals(actual)) {
                testContext.markAsFailed(
                        "SecHub report not as wanted. Expected was traffic light: " + expected + ", but result was: " + report.getTrafficLight(),
                        "SecHub report was:\n" + reportAsJson);
                return;
            }

        }
        if (sechubResultAssert.getEqualsFile().isPresent()) {
            AssertEqualsFileDefinition equalsFile = sechubResultAssert.getEqualsFile().get();
            String pathToTemplate = equalsFile.getPath();
            String template = null;
            try {
                template = Files.readString(Paths.get(pathToTemplate));
            } catch (IOException e) {
                throw new IllegalStateException("Was not able to read given test template file:" + pathToTemplate, e);
            }

            UUID secHubJobUUID = testContext.getSecHubRunData().getSecHubJobUUID();
            TestTemplateSupport templateSupport = new TestTemplateSupport();
            templateSupport.setSecHubJobUUID(secHubJobUUID);

            TemplateMatchResult matchResult = templateSupport.calculateTemplateMatching(template, reportAsJson);

            if (!matchResult.isMatching()) {
                testContext.markAsFailed("The SecHub report content was not equal/matched not the given test template file.",
                        "Changed template:\n" + matchResult.getTransformedTemplate() + "\n\nChanged report:\n" + matchResult.getTransformedContent());
                return;
            }

        }

        if (sechubResultAssert.getContainsStrings().isPresent()) {
            AssertContainsStringsDefinition containsStrings = sechubResultAssert.getContainsStrings().get();
            List<String> containingStrings = containsStrings.getValues();

            for (String stringWhichMustBeContained : containingStrings) {
                if (!reportAsJson.contains(stringWhichMustBeContained)) {
                    testContext.markAsFailed("The SecHub report did not contain expected string:" + stringWhichMustBeContained, "Report was:\n" + reportAsJson);
                }
            }
        }

    }

}
