// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.TestAbortedException;

import com.mercedesbenz.sechub.integrationtest.internal.TestScenario;

public class IntegrationTestExtension implements InvocationInterceptor {

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        if (!IntegrationTestSupport.isIntegrationTestingEnabled()) {

            invocation.skip();

            String message = "Integration test aborted because not enabled. To enable please define -D" + IntegrationTestSupport.SECHUB_INTEGRATIONTEST_RUNNING
                    + "=true to enable integration tests!";

            throw new TestAbortedException(message);
        }

        IntegrationTestSupport integrationTestSupport = null;
        Class<?> clazz = invocationContext.getTargetClass();
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof WithTestScenario) {
                WithTestScenario withTestScenario = (WithTestScenario) annotation;
                Class<? extends TestScenario> scenarioClass = withTestScenario.value();

                integrationTestSupport = IntegrationTestSupport.create(scenarioClass);
                break;
            }
        }

        if (integrationTestSupport == null) {
            fail("Integration test support was not created. Please add @WithTestScenario annotation at class level to test class !");
        }

        String testMethodName = invocationContext.getExecutable().getName();

        integrationTestSupport.executeTest(clazz, testMethodName, () -> invocation.proceed(), (text) -> Assumptions.assumeTrue(false, text));
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        proceedOrSkipInvocationDependingIntegrationTestsAreEnabled(invocation);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        proceedOrSkipInvocationDependingIntegrationTestsAreEnabled(invocation);
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        proceedOrSkipInvocationDependingIntegrationTestsAreEnabled(invocation);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        proceedOrSkipInvocationDependingIntegrationTestsAreEnabled(invocation);
    }

    private void proceedOrSkipInvocationDependingIntegrationTestsAreEnabled(Invocation<Void> invocation) throws Throwable {
        if (IntegrationTestSupport.isIntegrationTestingEnabled()) {
            invocation.proceed();
        } else {
            invocation.skip();
        }

    }

}
