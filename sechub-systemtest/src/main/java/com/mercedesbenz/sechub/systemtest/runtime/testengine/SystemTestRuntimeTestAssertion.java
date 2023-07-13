package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.api.SecHubReport;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
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
        if (!testContext.isSecHubTest()) {
            return;
        }
        List<AssertSechubResultDefinition> sechubResultAsserts = assertDefinition.getSechubResult();
        for (AssertSechubResultDefinition sechubResultAssert : sechubResultAsserts) {
            handleSecHubAssert(sechubResultAssert, testContext);
        }

    }

    private void handleSecHubAssert(AssertSechubResultDefinition sechubResultAssert, TestEngineTestContext testContext) {
        SecHubReport report = testContext.getSecHubRunData().getReport();
        String reportAsJson = JSONConverter.get().toJSON(report, true);

        if (sechubResultAssert.getHasTrafficLight().isPresent()) {
            TrafficLight expected = sechubResultAssert.getHasTrafficLight().get();
            if (!expected.equals(report.getTrafficLight())) {
                failWithMessage("SecHub report not as wanted. Expected was traffic light: " + expected + ", but result was: " + report.getTrafficLight()
                        + "\nSecHub report was:\n" + reportAsJson, testContext);
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
                failWithMessage("The SecHub report content was not equal/matched not the given test template file.\n\nReduced template:\n"
                        + matchResult.getChangedTemplate() + "\n\nGot reduced report:\n" + matchResult.getChangedContent(), testContext);
                return;
            }

        }

        if (sechubResultAssert.getContainsStrings().isPresent()) {
            AssertContainsStringsDefinition containsStrings = sechubResultAssert.getContainsStrings().get();
            List<String> containingStrings = containsStrings.getValues();

            for (String stringWhichMustBeContained : containingStrings) {
                if (!reportAsJson.contains(stringWhichMustBeContained)) {
                    failWithMessage("The SecHub report did not contain expected string:" + stringWhichMustBeContained + ".\n\nReport was:\n" + reportAsJson,
                            testContext);
                }
            }
        }

    }

    private void failWithMessage(String message, TestEngineTestContext testContext) {
        testContext.markAsFailed(message);
    }

}
