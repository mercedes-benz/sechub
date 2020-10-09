// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;
import com.daimler.sechub.integrationtest.internal.PDSTestScenario;
import com.daimler.sechub.integrationtest.internal.SecHubServerTestScenario;
import com.daimler.sechub.integrationtest.internal.TestRestHelper;
import com.daimler.sechub.integrationtest.internal.TestScenario;

import ch.qos.logback.classic.Level;

public class IntegrationTestSetup implements TestRule {

    private static final String SECHUB_INTEGRATIONTEST_LONG_RUNNING = "sechub.integrationtest.longrunning";
    static final String SECHUB_INTEGRATIONTEST_RUNNING = "sechub.integrationtest.running";
    private static final String SECHUB_INTEGRATIONTEST_CLIENT_DEBUG = "sechub.integrationtest.client.debug";
    private static final String SECHUB_INTEGRATIONTEST_ENABLE_HTTP_DEBUG_LOGGING = "sechub.integrationtest.enable.http.debug";

    private static final int DEFAULT_MILLISECONDS_TO_WAIT_FOR_PREPARATION = 300;
    private static final String SECHUB_INTEGRATIONTEST_WAIT_PREPARE_MILLISECONDS = "sechub.integrationtest.prepare.wait.ms";
    private static final String ENV_SECHUB_INTEGRATIONTEST_WAIT_PREPARE_MILLISECONDS = "SECHUB_INTEGRATIONTEST_PREPARE_WAIT_MS";

    public static final boolean SECHUB_CLIENT_DEBUGGING_ENABLED = Boolean.getBoolean(SECHUB_INTEGRATIONTEST_CLIENT_DEBUG);

    private TestScenario scenario;
    private boolean longrunning;
    private static String isServerAliveURL;
    private static String isPDSAliveURL;

    private static final int MAX_SECONDS_TO_WAIT_FOR_INTEGRATION_SERVER = 15;

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestSetup.class);

    private static Boolean testServerStatusCache = null;
    private static Boolean testPDSStatusCache;
    
    private static final Map<Class<? extends TestScenario>,TestScenario> scenarioClassToInstanceMap = new LinkedHashMap<>();

    /**
     * The next lines are absolute necessary stuff - why? Unfortunately apache http
     * client used for webmvc testing does enable debug logging always which does
     * create EXTREME output on console. And on some tests e.g. Uploading a 5 MB ZIP
     * file for test we got so much binary output which can lead to IDE console
     * problems.<br>
     * <br>
     * To prevent this we disable these loggers here. But can be turned on by system
     * property if really needed.
     */
    static {
        if (!Boolean.getBoolean(SECHUB_INTEGRATIONTEST_ENABLE_HTTP_DEBUG_LOGGING)) {
            Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));

            for (String log : loggers) {
                org.slf4j.Logger impl = LoggerFactory.getLogger(log);
                if (impl instanceof ch.qos.logback.classic.Logger) {
                    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) impl;
                    logger.setLevel(Level.INFO);
                    logger.setAdditive(false);
                } else {
                    impl.error(
                            "Cannot turn off the 'org.apache.http' or 'groovyx.net.http' logging so having much debug output in every test which can make problems in IDEs and/or slows down tests");
                }
            }
        } else {
            LOG.info("You have not turned http debug logging. So you will get much output");
        }
        isServerAliveURL = IntegrationTestContext.get().getUrlBuilder().buildIntegrationTestIsAliveUrl();
        isPDSAliveURL = IntegrationTestContext.get().getPDSUrlBuilder().buildIntegrationTestIsAliveUrl();
    }

    private IntegrationTestSetup(TestScenario scenario) {
        this.scenario = scenario;
    }

    public TestScenario getScenario() {
        return scenario;
    }
    /**
     * Marks this test setup as a long running variant. Means you have to define
     * additional SECHUB_INTEGRATIONTEST_LONG_RUNNING system property as true, to
     * have this test not ignored. Use this methods to mark extreme slow tests which
     * normally are not run because seldom used or maybe only by build servers
     * 
     * @return this
     */
    public IntegrationTestSetup markLongRunning() {
        this.longrunning = true;
        return this;
    }

    public static IntegrationTestSetup forScenario(Class<? extends TestScenario> scenarioClazz) {
        TestScenario scenario = scenarioClassToInstanceMap.computeIfAbsent(scenarioClazz, clazz -> {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Cannot create scenario instance for:" + scenarioClazz, e);
            }
        });
        return new IntegrationTestSetup(scenario);
        
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
            /* skip tests when not in integration test mode */
            boolean integrationTestEnabled;

            /*
             * we differ between long running and "normal" integration tests. If user wants
             * both executed, just two system properties must be set at execution time.
             * Otherwise a dedicated ones can be enabled
             */
            if (longrunning) {
                integrationTestEnabled = Boolean.getBoolean(SECHUB_INTEGRATIONTEST_LONG_RUNNING);
                if (!integrationTestEnabled) {
                    String message = "Skipped test scenario '" + scenario.getName() + "'\nReason: not in integration (long running )test mode.\nDefine -D"
                            + SECHUB_INTEGRATIONTEST_LONG_RUNNING + "=true to enable *long* running integration tests!";
                    Assume.assumeTrue(message, false);
                }
            } else {
                integrationTestEnabled = LocalDeveloperFileSetupSupport.INSTANCE.isAlwaysSecHubIntegrationTestRunning() || Boolean.getBoolean(SECHUB_INTEGRATIONTEST_RUNNING);
                if (!integrationTestEnabled) {
                    String message = "Skipped test scenario '" + scenario.getName() + "'\nReason: not in integration test mode.\nDefine -D"
                            + SECHUB_INTEGRATIONTEST_RUNNING + "=true to enable integration tests!";
                    Assume.assumeTrue(message, false);
                }
            } 
            assertNecessaryTestServersRunning();
           
            /* prepare */
            String testClass = description.getClassName();
            String testMethod = description.getMethodName();
            try {
                boolean sechubServerScenario = scenario instanceof SecHubServerTestScenario;
                if (sechubServerScenario) {
                    TestAPI.ensureNoLongerJobExecution();
                }
                String scenarioName = scenario.getName();
                LOG.info("############################################################################################################");
                LOG.info("###");
                LOG.info("### [START] Preparing scenario: '" + scenarioName+"'");
                LOG.info("###");
                LOG.info("############################################################################################################");
                LOG.info("###   Class ="+testClass);
                LOG.info("###   Method="+testMethod);
                LOG.info("############################################################################################################");
                
                scenario.prepare(testClass, testMethod);
                
                LOG.info("############################################################################################################");
                LOG.info("###");
                LOG.info("### [DONE ]  Preparing scenario: '" + scenarioName+"'");
                LOG.info("### [START]  Test itself");
                LOG.info("###");
                LOG.info("############################################################################################################");
                LOG.info("###   Class ="+testClass);
                LOG.info("###   Method="+testMethod);
                LOG.info("############################################################################################################");
                
                String info = "\n\n\n" + "  Start of integration test\n\n" + "  - Test class:" + testClass + "\n" + "  - Method:" + testMethod + "\n\n" + "\n";
                logInfo(info);

                if (sechubServerScenario) {
                    waitForPreparationEventsDone();
                }

            } catch (Throwable e) {
                LOG.error("#########################################################################");
                LOG.error("#");
                LOG.error("#         FATAL SCENARIO ERROR");
                LOG.error("#");
                LOG.error("#########################################################################");
                LOG.error("#    Wasnt able to prepare scenario:{}", scenario.getName());
                LOG.error("#########################################################################");
                LOG.error("Last url :" + TestRestHelper.getLastUrl());
                LOG.error("Last data:" + TestRestHelper.getLastData());

                logInfo("\n\n\nEnd of integration test (scenario failure):\n - Test class: " + testClass + "\n - Method:" + testMethod + "\n\n");

                throw e;
            }
            try {
                next.evaluate();
            } catch (HttpStatusCodeException e) {
                HttpStatus code = e.getStatusCode();

                String lastURL = TestRestHelper.getLastUrl();
                throw new IntegrationTestException(
                        "HTTP ERROR " + e.getRawStatusCode() + " '" + (code != null ? code.getReasonPhrase() : "?") + "', " + lastURL, e);
            } finally {
                logInfo("\n\n\nEnd of integration test:\n - Test class: " + testClass + "\n - Method:" + testMethod + "\n\n");
            }
        }

        private void logInfo(String info) {
            boolean sechubServerScenario = scenario instanceof SecHubServerTestScenario;
            boolean pdsServerScenario = scenario instanceof PDSTestScenario;
            if (sechubServerScenario) {
                TestAPI.logInfoOnServer(info);
            }
            if (pdsServerScenario) {
                TestAPI.logInfoOnPDS(info);
            }
        }

        private void waitForPreparationEventsDone() throws InterruptedException {
            // Callers can override this by environment entry or setting system property.

            int timeToWaitMilliseconds = DEFAULT_MILLISECONDS_TO_WAIT_FOR_PREPARATION;
            /* system property variant: */
            String timeToWait = System.getProperty(SECHUB_INTEGRATIONTEST_WAIT_PREPARE_MILLISECONDS);
            if (timeToWait != null) {
                timeToWaitMilliseconds = Integer.parseInt(timeToWait);
            }
            /* env setup variant: */
            timeToWait = System.getenv(ENV_SECHUB_INTEGRATIONTEST_WAIT_PREPARE_MILLISECONDS);
            if (timeToWait != null) {
                timeToWaitMilliseconds = Integer.parseInt(timeToWait);
            }
            LOG.info("Start waiting for preparation done: {} milliseconds", timeToWaitMilliseconds);
            Thread.sleep(timeToWaitMilliseconds);
        }

    }

    static class IntegrationTestException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public IntegrationTestException(String message, Exception cause) {
            super(message, cause);
        }

    }

    private static void assertTestServerRunning() { // NOSONAR - being static this is outside IntegrationTestStatement
        String url = isServerAliveURL;
        if (testServerStatusCache == null) {
            testServerStatusCache = fetchTestServerStatus(url);
        }
        if (!Boolean.TRUE.equals(testServerStatusCache)) {
            throw new IntegrationTestServerNotFoundException(
                    createTimeInfo("The integration test sechub server is not running. Cannot execute test. Build is alive url was:" + url));
        }
    }

    public void assertNecessaryTestServersRunning() {
        boolean sechubServerScenario = scenario instanceof SecHubServerTestScenario;
        boolean pdsServerScenario = scenario instanceof PDSTestScenario;
        /* checker server running */
        if (sechubServerScenario) {
            assertTestServerRunning();
        }
        if (pdsServerScenario) {
            assertTestPDSRunning();
        }
        
    }

    private static void assertTestPDSRunning() { // NOSONAR - being static this is outside IntegrationTestStatement
        String url = isPDSAliveURL;
        if (testPDSStatusCache == null) {
            testPDSStatusCache = fetchTestServerStatus(url);
        }
        if (!Boolean.TRUE.equals(testPDSStatusCache)) {
            throw new IntegrationTestServerNotFoundException(
                    createTimeInfo("The integration test PDS is not running. Cannot execute test. Build is alive url was:" + url));
        }
    }

    public static class IntegrationTestServerNotFoundException extends IllegalStateException {

        private static final long serialVersionUID = 1030904546816108076L;

        public IntegrationTestServerNotFoundException(String message) {
            super(message);
        }

    }

    private static final String createTimeInfo(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append("\nTime was:");
        sb.append(LocalDateTime.now());
        return sb.toString();
    }

    /**
     * Waits for test server
     *
     * @return
     */
    public static Boolean fetchTestServerStatus(String isAliveUrl) {

        for (int i = 0; i < MAX_SECONDS_TO_WAIT_FOR_INTEGRATION_SERVER; i++) {
            try {
                IntegrationTestContext.get().getRestHelper(TestAPI.ANONYMOUS).getStringFromURL(isAliveUrl);
                return Boolean.TRUE;
            } catch (ResourceAccessException e) {
                try {
                    /* NOSONAR */Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    return Boolean.FALSE;
                }
            } catch (Exception e) {
                LOG.error("Implementation failure, test framework problems", e);/* we cannot give back a correct answer, so throwing an illegal state here */
                return Boolean.FALSE;
            }
        }
        LOG.warn("Was not able to get access to integrationtest server");
        return Boolean.FALSE;
    }

}
