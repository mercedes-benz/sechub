// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.mercedesbenz.sechub.integrationtest.internal.TestScenario;

/**
 * Old junit4 way to setup integration tests. Please do not longer use them
 * inside new integration tests but instead use the new Junit5 way via :
 * {@link IntegrationTestExtension}.
 *
 * (Older integration tests using this test rule will be migrated step by step
 * in future)
 */
public class IntegrationTestSetup implements TestRule {

    private IntegrationTestSupport integrationTestSupport;

    private IntegrationTestSetup(IntegrationTestSupport baseSetup) {
        this.integrationTestSupport = baseSetup;
    }

    public TestScenario getScenario() {
        return integrationTestSupport.getScenario();
    }

    public static IntegrationTestSetup forScenario(Class<? extends TestScenario> scenarioClazz) {
        return new IntegrationTestSetup(IntegrationTestSupport.create(scenarioClazz));

    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new IntegrationTestStatement(base, description);
    }

    private class IntegrationTestStatement extends Statement {

        private final Statement next;
        private Description description;

        public IntegrationTestStatement(Statement base, Description description) {
            next = base;
            this.description = description;
        }

        @Override
        public void evaluate() throws Throwable {

            Class<?> testClass = description.getTestClass();
            String testMethod = description.getMethodName();

            integrationTestSupport.executeTest(testClass, testMethod, () -> next.evaluate(), (text) -> Assume.assumeTrue(text, false));
        }

    }

}
