package com.mercedesbenz.sechub.integrationtest.api;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.integrationtest.internal.NoSecHubSuperAdminNecessaryScenario;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.TestRestHelper;
import com.mercedesbenz.sechub.integrationtest.internal.TestScenario;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;

import ch.qos.logback.classic.Level;

public class IntegrationTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestSupport.class);

    static final LocalDeveloperFileSetupSupport support = LocalDeveloperFileSetupSupport.INSTANCE;
    static final String SECHUB_INTEGRATIONTEST_ONLY_NECESSARY_TESTS_FOR_DOCUMENTATION = "sechub.integrationtest.only.necessary4documentation";
    static final String SECHUB_INTEGRATIONTEST_NEVER_NECESSARY_TESTS_FOR_DOCUMENTATION = "sechub.integrationtest.never.necessary4documentation";

    static final String SECHUB_INTEGRATIONTEST_LONG_RUNNING = "sechub.integrationtest.longrunning";
    static final String SECHUB_INTEGRATIONTEST_RUNNING = "sechub.integrationtest.running";
    static final String SECHUB_INTEGRATIONTEST_CLIENT_DEBUG = "sechub.integrationtest.client.debug";
    static final String SECHUB_INTEGRATIONTEST_ENABLE_HTTP_DEBUG_LOGGING = "sechub.integrationtest.enable.http.debug";

    static final int DEFAULT_MILLISECONDS_TO_WAIT_FOR_PREPARATION = 300;
    static final String SECHUB_INTEGRATIONTEST_WAIT_PREPARE_MILLISECONDS = "sechub.integrationtest.prepare.wait.ms";
    static final String ENV_SECHUB_INTEGRATIONTEST_WAIT_PREPARE_MILLISECONDS = "SECHUB_INTEGRATIONTEST_PREPARE_WAIT_MS";

    public static final boolean SECHUB_CLIENT_DEBUGGING_ENABLED = Boolean.getBoolean(SECHUB_INTEGRATIONTEST_CLIENT_DEBUG);

    private static String isServerAliveURL;
    private static String isPDSAliveURL;

    private static final int MAX_SECONDS_TO_WAIT_FOR_INTEGRATION_SERVER = 15;
    private static final int MAX_SECONDS_TO_WAIT_FOR_SUPER_ADMIN_AVAILABLE = 10;

    private static Boolean testServerStatusCache;
    private static Boolean testPDSStatusCache;
    private static Boolean testInternalAdminAvailableCache;

    private static final Map<Class<? extends TestScenario>, TestScenario> scenarioClassToInstanceMap = new LinkedHashMap<>();

    private static boolean criticalIntegrationSetupProblemDetected;
    private static String criticalProblem;
    private static String criticalTestClass;

    private TestScenario scenario;

    private boolean longrunning;

    private IntegrationTestSupport(TestScenario scenario) {
        this.scenario = scenario;
    }

    public TestScenario getScenario() {
        return scenario;
    }

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

    static class IntegrationTestException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public IntegrationTestException(String message, Exception cause) {
            super(message, cause);
        }

    }

    private static void assertTestAPIInternalSuperAdminAvailable(TestScenario scenario) { // NOSONAR - being static this is outside IntegrationTestStatement
        if (scenario instanceof NoSecHubSuperAdminNecessaryScenario) {
            /* in this case we do not care about the super admin because not necessary. */
            return;
        }
        if (testInternalAdminAvailableCache == null) {
            testInternalAdminAvailableCache = testAPIableToListSignupsByInternalUsedSuperAdmin();
        }
        if (!Boolean.TRUE.equals(testInternalAdminAvailableCache)) {
            throw new IntegrationTestAdminNotAvailableException(createTimeInfo("The integration test admin is not available. Cannot execute test."));
        }
    }

    private static void assertTestServerRunning() { // NOSONAR - being static this is outside IntegrationTestStatement
        String url = isServerAliveURL;
        if (testServerStatusCache == null) {
            testServerStatusCache = fetchTestServerStatus(url);
        }
        if (!Boolean.TRUE.equals(testServerStatusCache)) {
            throw new IntegrationTestServerNotFoundException(
                    createTimeInfo("The integration test sechub server is not running. Cannot execute test. Used 'is alive url': " + url));
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

    public static class IntegrationTestAdminNotAvailableException extends IllegalStateException {

        private static final long serialVersionUID = 1030904546816108076L;

        public IntegrationTestAdminNotAvailableException(String message) {
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

    /*
     * Waits for internal super admin can access signups - means internal user
     * exists has super admin rights and access.
     */
    private static Boolean testAPIableToListSignupsByInternalUsedSuperAdmin() {
        for (int i = 0; i < MAX_SECONDS_TO_WAIT_FOR_SUPER_ADMIN_AVAILABLE; i++) {
            try {
                TestAPI.listSignups();
                LOG.debug("TestAPI is able to list signups - super admin is available");
                return Boolean.TRUE;
            } catch (Exception e) {
                try {
                    /* NOSONAR */Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    return Boolean.FALSE;
                }
            }
        }
        LOG.warn("TestAPI is NOT able to list signups - so super admin is NOT available or has not correct permissions!");
        return Boolean.FALSE;
    }

    private enum TestTag {

        DONE(null),

        SCENARIO_FAILURE("scenario failure");

        private String text;

        private TestTag(String text) {
            this.text = text;
        }
    }

    private static TestScenario resolveScenarioFromCacheOrCreate(Class<? extends TestScenario> scenarioClazz) {
        TestScenario scenario = scenarioClassToInstanceMap.computeIfAbsent(scenarioClazz, clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                    | SecurityException e) {
                throw new IllegalStateException("Cannot create scenario instance for:" + scenarioClazz, e);
            }
        });
        return scenario;
    }

    public static IntegrationTestSupport create(Class<? extends TestScenario> scenarioClazz) {
        return new IntegrationTestSupport(resolveScenarioFromCacheOrCreate(scenarioClazz));
    }

    public void executeTest(Class<?> testClass, String testMethodName, ProceedHandler proceedHandler, TestSkipper testSkipper) throws Throwable {
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
                testSkipper.skipTest(message);
            }
        } else {
            integrationTestEnabled = support.isAlwaysSecHubIntegrationTestRunning() || Boolean.getBoolean(SECHUB_INTEGRATIONTEST_RUNNING);
            if (!integrationTestEnabled) {
                String message = "Skipped test scenario '" + scenario.getName() + "'\nReason: not in integration test mode.\nDefine -D"
                        + SECHUB_INTEGRATIONTEST_RUNNING + "=true to enable integration tests!";
                testSkipper.skipTest(message);
            }
        }

        if (criticalIntegrationSetupProblemDetected) {
            // in this case we cannot test anything else any longer
            throw new IllegalStateException("CRITICAL integration setup problem detected:\nProblem:" + criticalProblem
                    + "\nTestClass where critical error happend:" + criticalTestClass + "\nLook into console output for more details");
        }

        String testClassName = testClass.getName();

        handleDocumentationTestsSpecialIfWanted(testClass, testSkipper);
        assertNecessaryTestServersRunning();
        assertTestAPIInternalSuperAdminAvailable(scenario);

        long startTime = 0;
        /* prepare */
        try {
            String scenarioName = scenario.getName();
            LOG.info("############################################################################################################");
            LOG.info("###");
            LOG.info("### [START] Preparing scenario: '" + scenarioName + "'");
            LOG.info("###");
            LOG.info("############################################################################################################");
            LOG.info("###   Class =" + testClassName);
            LOG.info("###   Method=" + testMethodName);
            LOG.info("############################################################################################################");

            boolean sechubServerScenario = scenario instanceof SecHubServerTestScenario;
            if (sechubServerScenario) {
                TestAPI.ensureNoLongerJobExecution();
            }

            scenario.prepare(testClassName, testMethodName);

            LOG.info("############################################################################################################");
            LOG.info("###");
            LOG.info("### [DONE ]  Preparing scenario: '" + scenarioName + "'");
            LOG.info("### [START]  Test itself");
            LOG.info("###");
            LOG.info("############################################################################################################");
            LOG.info("###   Class =" + testClassName);
            LOG.info("###   Method=" + testMethodName);
            LOG.info("############################################################################################################");

            String info = "\n\n\n" + "  Start of integration test\n\n" + "  - Test class:" + testClass + "\n" + "  - Method:" + testMethodName + "\n\n" + "\n";
            logInfo(info);
            startTime = System.currentTimeMillis();
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
            LOG.error("#    Reason: {}", e.getMessage());
            LOG.error("#    (for more details look in unit test stack trace output)");
            LOG.error("#########################################################################");
            LOG.error("Last url :" + TestRestHelper.getLastUrl());
            LOG.error("Last data:" + TestRestHelper.getLastData());

            logEnd(TestTag.SCENARIO_FAILURE, testClassName, testMethodName, startTime);

            if (e instanceof CriticalTestProblemException) {
                criticalProblem = e.getMessage();
                criticalTestClass = "Integration test scenario setup critical failure in " + scenario.getName();
                criticalIntegrationSetupProblemDetected = true;
            }

            throw e;
        }
        try {
//          junit 4:   next.evaluate();
            proceedHandler.proceed();
        } catch (HttpStatusCodeException e) {
            HttpStatusCode code = e.getStatusCode();

            String lastURL = TestRestHelper.getLastUrl();
            throw new IntegrationTestException("HTTP ERROR " + code + " '" + (code != null ? e.getStatusText() : "?") + "', " + lastURL, e);
        } catch (CriticalTestProblemException e) {
            criticalProblem = e.getMessage();
            criticalIntegrationSetupProblemDetected = true;
            criticalTestClass = testClassName;

            LOG.error("############################################################################################################");
            LOG.error("###");
            LOG.error("### [CRITICAL] - Critical test problem detected. Will now fail all other tests without execution");
            LOG.error("###              because test results will be no longer valid or cannot be executed any longer!");
            LOG.error("###");
            LOG.error("############################################################################################################");
            LOG.error("###   Class  =" + testClassName);
            LOG.error("###   Method =" + testMethodName);
            LOG.error("###   Problem=" + criticalProblem);
            LOG.error("############################################################################################################");

        } finally {
            logEnd(TestTag.DONE, testClassName, testMethodName, startTime);
        }
    }

    private void handleDocumentationTestsSpecialIfWanted(Class<?> testClass, TestSkipper testSkipper) {
        boolean onlyWhenNecessaryDocumentationTest = Boolean.getBoolean(SECHUB_INTEGRATIONTEST_ONLY_NECESSARY_TESTS_FOR_DOCUMENTATION);
        boolean neverWhenNecessaryDocumentationTest = Boolean.getBoolean(SECHUB_INTEGRATIONTEST_NEVER_NECESSARY_TESTS_FOR_DOCUMENTATION);

        if (!onlyWhenNecessaryDocumentationTest && !neverWhenNecessaryDocumentationTest) {
            return;
        }

        /* sanity check */
        if (onlyWhenNecessaryDocumentationTest && neverWhenNecessaryDocumentationTest) {
            String message = "Either define \n-D" + SECHUB_INTEGRATIONTEST_ONLY_NECESSARY_TESTS_FOR_DOCUMENTATION + "=true\nor\n-D"
                    + SECHUB_INTEGRATIONTEST_NEVER_NECESSARY_TESTS_FOR_DOCUMENTATION + "=true\nBut not both!";
            throw new IllegalStateException(message);
        }
        boolean isADocumentationTest = false;
        if (TestIsNecessaryForDocumentation.class.isAssignableFrom(testClass)) {
            isADocumentationTest = true;
        }
        if (onlyWhenNecessaryDocumentationTest && !isADocumentationTest) {
            String message = "Skipped test because not necessary for documentation and currently only those necessary tests are executed.";
            testSkipper.skipTest(message);
        }
        if (neverWhenNecessaryDocumentationTest && isADocumentationTest) {
            String message = "Skipped test because necessary for documentation and currently we do NOT execute those tests but all others.";
            testSkipper.skipTest(message);
        }

    }

    private void logEnd(TestTag failureTag, String testClass, String testMethod, long startTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\nEnd of integration test:\n - Test  : class ");
        sb.append(testClass);
        if (failureTag.text != null) {
            sb.append('(');
            sb.append(failureTag.text);
            sb.append(')');
        }
        sb.append("\n - Method: ");
        sb.append(testMethod);
        sb.append("\n - Time  : ");
        sb.append(elapsed(startTime) + " ms from start of actual test until end\n\n");

        logInfo(sb.toString());
    }

    private long elapsed(long startTime) {
        return System.currentTimeMillis() - startTime;
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

    interface ProceedHandler {
        void proceed() throws Throwable;
    }

    interface TestSkipper {
        void skipTest(String resonForSkip);
    }

}
