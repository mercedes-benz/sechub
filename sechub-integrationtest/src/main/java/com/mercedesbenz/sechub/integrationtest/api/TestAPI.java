// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import junit.framework.AssertionFailedError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.integrationtest.internal.DefaultTestExecutionProfile;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;
import com.mercedesbenz.sechub.integrationtest.internal.TestRestHelper;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogData;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingData;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingEntry;
import com.mercedesbenz.sechub.sharedkernel.messaging.IntegrationTestEventHistory;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestURLBuilder;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfile;

public class TestAPI {

    private static final String INTEGRATIONTEST_CHECK_SCANCONFIG_REFRESH_PROVIDERID = "integrationtest.check.scanconfig.refresh.providerid";

    private static final Logger LOG = LoggerFactory.getLogger(TestAPI.class);

    public static File PDS_WORKSPACE_FOLDER = new File("./build/test-results/pds-runtime/workspace");

    /**
     * Do <b>NOT</b> change this user in tests! This is only for checks. Only
     * special scenario users are automatically reverted
     */
    public static final TestUser ANONYMOUS = new FixedTestUser();

    /**
     * Do <b>NOT</b> change this user in tests! This is only for checks. Only
     * special scenario users are automatically reverted
     */
    public static final TestUser SUPER_ADMIN = new FixedTestUser("int-test_superadmin", "int-test_superadmin-pwd",
            "superadmin@" + ExampleConstants.URI_SECHUB_SERVER);
    /**
     * Do <b>NOT</b> change this user in tests! This is only for checks. Only
     * special scenario users are automatically reverted
     */
    public static final TestUser ONLY_USER = new FixedTestUser("int-test_onlyuser", "int-test_onlyuser-pwd", "onlyuser@" + ExampleConstants.URI_TARGET_SERVER);

    /**
     * Technical user used for communication with integration test PDS
     */
    public static final TestUser PDS_TECH_USER = new FixedTestUser("pds-inttest-techuser", "pds-inttest-apitoken",
            "pds_techuser@" + ExampleConstants.URI_TARGET_SERVER);

    /**
     * Admin account used for communication with integration test PDS
     */
    public static final TestUser PDS_ADMIN = new FixedTestUser("pds-inttest-admin", "pds-inttest-apitoken", "pds_admin@" + ExampleConstants.URI_TARGET_SERVER);

    private static final long MAXIMUM_WAIT_FOR_RUNNING_JOBS = 300 * 1000;// 300 seconds = 5 minutes max;

    public static final AsUser as(TestUser user) {
        return new AsUser(user);
    }

    public static final AsPDSUser asPDSUser(TestUser user) {
        return new AsPDSUser(user);
    }

    @Deprecated // use assertReport instead (newer implementation , has more details and uses
                // common SecHubReport object inside)
    public static AssertSecHubReport assertSecHubReport(String json) {
        return AssertSecHubReport.assertSecHubReport(json);
    }

    public static AssertReport assertReport(String json) {
        return AssertReport.assertReport(json);
    }

    public static AssertFullScanData assertFullScanDataZipFile(File file) {
        return AssertFullScanData.assertFullScanDataZipFile(file);
    }

    public static AssertPDSStatus assertPDSJobStatus(String json) {
        return new AssertPDSStatus(json);
    }

    public static AssertPDSCreateJobResult assertPDSJobCreateResult(String json) {
        return new AssertPDSCreateJobResult(json);
    }

    public static AssertPDSWorkspace assertPDSWorkspace() {
        return new AssertPDSWorkspace();
    }

    public static AssertUser assertUser(TestUser user) {
        return new AssertUser(user);
    }

    public static AssertSignup assertSignup(TestUser user) {
        return new AssertSignup(user);
    }

    public static AssertProject assertProject(TestProject project) {
        return new AssertProject(project);
    }

    public static AssertJSON assertJSON(String json) {
        return AssertJSON.assertJson(json);
    }

    public static AssertSecurityLog assertSecurityLog() {
        return AssertSecurityLog.assertSecurityLog();
    }

    /**
     * Creates an assert object to inspect meta data
     *
     * @return
     */
    public static AssertInspections assertInspections() {
        return new AssertInspections();
    }

    public static void logInfoOnServer(String text) {
        String url = getURLBuilder().buildIntegrationTestLogInfoUrl();
        getContext().getRestHelper(ANONYMOUS).postPlainText(url, text);
    }

    public static void logInfoOnPDS(String text) {
        String url = getPDSURLBuilder().buildIntegrationTestLogInfoUrl();
        getContext().getPDSRestHelper(ANONYMOUS).postPlainText(url, text);
    }

    public static String getPDSStoragePathForJobUUID(UUID jobUUID) {
        String url = getPDSURLBuilder().pds().buildIntegrationTestCheckStoragePath(jobUUID);
        return getContext().getPDSRestHelper(ANONYMOUS).getStringFromURL(url);
    }

    /**
     * Waits for sechub job being done (means status execution result is OK) - after
     * 5 seconds time out is reached. When job is in state "failing" it will be
     * ignored and retried until time out or "success" state reached. This method
     * should normally only be used in test cases where a former job has been
     * canceled
     *
     * @param project
     * @param jobUUID
     */
    public static void waitForJobDoneAndEvenWaitWhileJobIsFailing(TestProject project, UUID jobUUID) {
        waitForJobDone(project, jobUUID, 5, false);
    }

    /**
     * Waits for sechub job being done (means status execution result is OK) - after
     * 5 seconds time out is reached. When job is in state "failing" it will be
     * ignored and retried until time out or "success" state reached.
     *
     * @param project
     * @param jobUUID
     */
    public static void waitForJobDoneAndFailWhenJobIsFailing(TestProject project, UUID jobUUID) {
        waitForJobDone(project, jobUUID, 5, true);
    }

    /**
     * Waits for SecHub job being done (means status execution result is OK)- after
     * 5 seconds time out is reached.
     *
     * @param project          project being inspected
     * @param jobUUID          job uuid to inspect status
     * @param timeOutInSeconds time out in seconds when no retry is possible any
     *                         more
     * @param jobMayNeverFail  when <code>true</code> the first job result in state
     *                         "failing" will automatically stop inspection and let
     *                         the test fail. This can be useful have faster
     *                         feedback and more details about the returned job
     *                         status (which is printed out in test log).
     */
    @SuppressWarnings("unchecked")
    public static void waitForJobDone(TestProject project, UUID jobUUID, int timeOutInSeconds, boolean jobMayNeverFail) {
        LOG.debug("wait for job done project:{}, job:{}", project.getProjectId(), jobUUID);

        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, timeOutInSeconds, HttpClientErrorException.class) {

            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                TestSecHubJobStatus jobStatus = getSecHubJobStatus(project, jobUUID, getUser());

                if (jobMayNeverFail && jobStatus.hasResultFailed()) {
                    String prettyJSON = JSONConverter.get().toJSON(jobStatus, true);
                    fail("The job execution has failed - skip further attempts to check that job will be done.\n-Status data:\n" + prettyJSON
                            + "\n\n- Please refer to server and/or PDS logs for reason.");
                }
                return jobStatus.hasResultOK();
            }

        });
    }

    public static TestSecHubJobStatus getSecHubJobStatus(TestProject project, UUID jobUUID, TestUser asUser) {
        String status = as(asUser).getJobStatus(project.getProjectId(), jobUUID);
        LOG.debug(">>>>>>>>>JOB:STATUS:" + status);
        TestSecHubJobStatus jobStatus = TestSecHubJobStatus.fromJSON(status);
        return jobStatus;
    }

    /**
     * Wait until SecHub job is running - after 5 seconds time out is reached
     *
     * @param project
     * @param jobUUID
     */
    public static void waitForJobRunning(TestProject project, UUID jobUUID) {
        waitForJobRunning(project, 5, 300, jobUUID);
    }

    /**
     * Wait until SecHub job is running
     *
     * @param project
     * @param timeOutInSeconds
     * @param timeToWaitInMillis
     * @param jobUUID
     */
    @SuppressWarnings("unchecked")
    public static void waitForJobRunning(TestProject project, int timeOutInSeconds, int timeToWaitInMillis, UUID jobUUID) {
        LOG.debug("wait for job running project:{}, job:{}, timeToWaitInMillis{}, timeOutInSeconds:{}", project.getProjectId(), jobUUID, timeToWaitInMillis,
                timeOutInSeconds);

        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, timeOutInSeconds, timeToWaitInMillis, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                String status = as(getUser()).getJobStatus(project.getProjectId(), jobUUID);
                LOG.debug(">>>>>>>>>JOB:STATUS:" + status);
                return status.contains("STARTED");
            }
        });
    }

    /**
     * Waits for sechub job being cancele requested - after 5 seconds time out is
     * reached
     *
     * @param project
     * @param jobUUID
     */
    @SuppressWarnings("unchecked")
    public static void waitForJobStatusCancelRequested(TestProject project, UUID jobUUID) {
        LOG.debug("wait for job cancel requested project:{}, job:{}", project.getProjectId(), jobUUID);

        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, 5, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                String status = as(getUser()).getJobStatus(project.getProjectId(), jobUUID);
                LOG.debug(">>>>>>>>>JOB:STATUS:" + status);
                return status.contains("CANCEL_REQUESTED");
            }
        });
    }

    /**
     * Waits for sechub job being failed - after 5 seconds time out is reached
     *
     * @param project
     * @param jobUUID
     */
    @SuppressWarnings("unchecked")
    public static void waitForJobStatusFailed(TestProject project, UUID jobUUID) {
        LOG.debug("wait for job failed project:{}, job:{}", project.getProjectId(), jobUUID);

        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, 5, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                String status = as(getUser()).getJobStatus(project.getProjectId(), jobUUID);
                LOG.debug(">>>>>>>>>JOB:STATUS:" + status);
                return status.contains("FAILED");
            }
        });
    }

    private static boolean notExceeded(long maxMilliseconds, long start) {
        return System.currentTimeMillis() - start < maxMilliseconds;
    }

    public static void executeUntilSuccessOrTimeout(TestExecutable testExecutable) {
        long start = System.currentTimeMillis();
        int maxMilliseconds = testExecutable.getTimeoutInSeconds() * 1000;
        do {
            boolean runWasSuccessful = false;
            try {
                runWasSuccessful = testExecutable.runAndReturnTrueWhenSuccesful();
            } catch (Exception exception) {
                /* ignore */
                boolean handled = false;
                for (Class<? extends Exception> handledException : testExecutable.getHandledExceptions()) {
                    if (exception.getClass().isAssignableFrom(handledException)) {
                        handled = true;
                        break;
                    }
                }
                if (!handled) {
                    throw new IllegalStateException("An unexpected / unhandled exception occurred at execution time!", exception);
                }
            }
            if (runWasSuccessful) {
                return;
            }

            /* not successful, so please wait */
            if (testExecutable.getTimeToWaitInMillis() > 0) {
                try {
                    Thread.sleep(testExecutable.getTimeToWaitInMillis());
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        } while (notExceeded(maxMilliseconds, start));

        /* was not possible to execute succesful in given time range */
        fail("Timeout of waiting for successful execution - waited " + testExecutable.getTimeoutInSeconds() + " seconds");
        return;
    }

    public static void executeRunnableAndAcceptAssertionsMaximumTimes(int tries, Runnable runnable, int millisBeforeNextRetry) {
        executeCallableAndAcceptAssertionsMaximumTimes(tries, () -> {
            runnable.run();
            return null;
        }, millisBeforeNextRetry);
    }

    public static <T> T executeCallableAndAcceptAssertionsMaximumTimes(int tries, Callable<T> assertionCallable, int millisBeforeNextRetry) {
        T result = null;
        AssertionFailedError failure = null;
        for (int i = 0; i < tries && failure == null; i++) {
            try {
                if (i > 0) {
                    /* we wait before next check */
                    TestAPI.waitMilliSeconds(millisBeforeNextRetry);
                }
                result = assertionCallable.call();
            } catch (AssertionFailedError e) {
                failure = e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (failure != null) {
            throw failure;
        }
        return result;
    }

    /**
     * As anonymous user one time token link is called and the resulting token is
     * set to given test user, so tests can continue without additional setup...
     *
     * @param user
     * @param link
     * @return
     */
    public static String udpdateAPITokenByOneTimeTokenLink(TestUser user, String link) {
        LOG.debug("update api token by one time token link for user:{}, link:{}", user.getUserId(), link);
        String newToken = as(ANONYMOUS).getStringFromURL(link);
        user.updateToken(newToken);
        return newToken;
    }

    /**
     * Returns link to fetch a new api token, after a signup was acepted. Will use
     * last sent mail body to determine the token.
     *
     * @param user
     * @return link, never empty.
     * @throws AssertionFailedError when no link available
     */
    public static String getLinkToFetchNewAPITokenAfterSignupAccepted(TestUser user) {
        LOG.debug("Get link to fetch new api token after signup accepted for for user:{}", user.getUserId());
        MockEmailEntry mail = IntegrationTestContext.get().emailAccess().findMailOrFail(user, "SecHub user account created");
        String text = mail.text.trim(); // remove last \n if existing...
        String[] lines = text.split("\n");

        String linkOfOneApiToken = lines[lines.length - 1];
        if (linkOfOneApiToken.isEmpty()) {
            fail("empty link line, origin text mail was:\n" + text);
        }
        return linkOfOneApiToken;
    }

    /**
     * Returns link to fetch a new api token, after a token change was requested.
     * Will use last sent mail body to determine the token.
     *
     * @param user
     * @return link, never empty.
     * @throws AssertionFailedError when no link available
     */
    public static String getLinkToFetchNewAPITokenAfterChangeRequest(TestUser user) {
        LOG.debug("Get link to fetch new api token after change requested for user:{}", user.getUserId());
        MockEmailEntry mail = IntegrationTestContext.get().emailAccess().findMailOrFail(user, "Your request for a new SecHub API token");
        String text = mail.text.trim(); // remove last \n if existing...
        String[] lines = text.split("\n");

        String linkOfOneApiToken = lines[lines.length - 1];
        if (linkOfOneApiToken.isEmpty()) {
            fail("empty link line, origin text mail was:\n" + text);
        }
        return linkOfOneApiToken;
    }

    /**
     * Expects an http failure when runnable is executed. If this does not happen,
     * dedicated error messages comes up and unit test will fail.
     *
     * @param expectedStatusCode
     * @param runnable
     */
    public static void expectHttpFailure(Runnable runnable, HttpStatus... expected) {
        expectHttpFailure(runnable, -1, expected);
    }

    /**
     * Expects an http failure when runnable is executed. If this does not happen,
     * dedicated error messages comes up and unit test will fail.
     *
     * @param expectedStatusCode
     * @param timeOutInMilliseconds as long this time out is not reached HTTP 200
     *                              messages will be ignored and after a short break
     *                              the runnnable wille be called again to provoke
     *                              expected failure.
     * @param runnable
     */
    public static void expectHttpFailure(Runnable runnable, long timeOutInMilliseconds, HttpStatus... expected) {
        if (expected == null || expected.length == 0) {
            throw new IllegalArgumentException("test case corrupt please add at least one expected error!");
        }
        /* sanity check: 20x is no HTTP failure... */
        assertNoHttp20xInside(expected);

        long start = System.currentTimeMillis();
        boolean timeElapsed = false;
        while (!timeElapsed) { /* NOSONAR */
            long waitedTimeInMilliseconds = System.currentTimeMillis() - start;
            timeElapsed = waitedTimeInMilliseconds > timeOutInMilliseconds;

            boolean failedAsExpected = false;
            try {
                runnable.run();
                if (timeElapsed) {
                    fail("No rest client exception - so user at least got a HTTP 20x what is wrong! Timeout reached:" + waitedTimeInMilliseconds + "/"
                            + timeOutInMilliseconds + " ms.");
                }
                int wait = 500;
                LOG.debug("Expected HTTP failure did not occure. Timeout not reached:" + waitedTimeInMilliseconds + "/" + timeOutInMilliseconds
                        + " ms. So Wait " + wait + " ms and retry");
                TestAPI.waitMilliSeconds(wait);
            } catch (HttpStatusCodeException he) {
                int status = he.getRawStatusCode();
                failedAsExpected = isAllowed(status, expected);
                if (failedAsExpected) {
                    return;
                }
                fail("Expected http status codes were:" + Arrays.asList(expected) + " but was " + status + "\nMessage:" + he.getMessage() + ",\nContent:"
                        + he.getResponseBodyAsString());
            } catch (RestClientException e) {
                fail("Expected a " + HttpStatusCodeException.class.getSimpleName() + " but was " + e.getClass());
            }
        }

    }

    private static boolean isAllowed(int status, HttpStatus... allowed) {
        for (HttpStatus expectedStatusCode : allowed) {
            if (expectedStatusCode.value() == status) {
                return true;
            }
        }
        return false;
    }

    private static void assertNoHttp20xInside(HttpStatus... expectedStatusCodes) {
        for (HttpStatus expectedStatusCode : expectedStatusCodes) {
            if (expectedStatusCode.is2xxSuccessful()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Wrong implemented unit test!\n");
                sb.append("You defined an expected status code inside your test which belongs to 2xxSuccesful familiy:\n");
                sb.append(expectedStatusCode.getReasonPhrase());
                sb.append("\n");
                sb.append("This status is never a failure - so your test is wrong implemented !");
                throw new IllegalArgumentException(sb.toString());
            }
        }
    }

    /**
     * Get uploaded file (means download former uploaded file) from integration test
     * server
     *
     * @param project
     * @param jobUUID
     * @param fileName
     * @return file or <code>null</code> when not found
     * @throws IllegalStateException when other problems are occurring
     */
    public static File getFileUploaded(TestProject project, UUID jobUUID, String fileName) {
        TestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildGetFileUpload(project.getProjectId(), jobUUID.toString(), fileName);
        try {
            File file = as(ANONYMOUS).downloadAsTempFileFromURL(url, jobUUID);
            return file;
        } catch (HttpStatusCodeException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                /* okay, just not existing ... */
                return null;
            }
            throw new IllegalStateException("Odd status code:" + e.getStatusCode() + ", message:" + e.getMessage(), e);
        }
    }

    public static String createSHA256Of(File uploadFile) {

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("should not happen:", e);
        }

        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(uploadFile), md)) {
            while (dis.read() != -1)
                ; // empty loop to clear the data
            md = dis.getMessageDigest();
        } catch (IOException e) {
            throw new IllegalStateException("should not happen:", e);
        }
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }

    public static void waitSeconds(int seconds) {
        waitMilliSeconds(seconds * 1000);
    }

    public static void waitMilliSeconds(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    /**
     * Changes scan mapping DIRECTLY ! Means without administration domain, but
     * directly in scan domain - interesting for testing only,
     *
     * @param json
     */
    public static void changeScanMappingDirectly(String mappingId, MappingEntry... entries) {
        MappingData data = new MappingData();
        for (MappingEntry entry : entries) {
            data.getEntries().add(entry);
        }
        TestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildIntegrationTestChangeMappingDirectlyURL(mappingId);

        IntegrationTestContext.get().getRestHelper(ANONYMOUS).putJSON(url, data.toJSON());

    }

    public static MappingData fetchMappingDataDirectlyOrNull(String mappingId) {

        TestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildIntegrationTestFetchMappingDirectlyURL(mappingId);

        String result = IntegrationTestContext.get().getRestHelper(ANONYMOUS).getJSON(url);
        if (result == null) {
            return null;
        }
        MappingData data = MappingData.fromString(result);
        return data;

    }

    public static void clearSecurityLogs() {
        TestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildIntegrationTestClearSecurityLogs();

        IntegrationTestContext.get().getRestHelper(ANONYMOUS).delete(url);
    }

    public static List<SecurityLogData> getSecurityLogs() {
        TestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildIntegrationTestGetSecurityLogs();

        String json = IntegrationTestContext.get().getRestHelper(ANONYMOUS).getJSON(url);
        ObjectMapper mapper = TestJSONHelper.get().getMapper();
        ObjectReader readerForListOf = mapper.readerForListOf(SecurityLogData.class);
        try {
            return readerForListOf.readValue(json);
        } catch (Exception e) {
            throw new IllegalStateException("was not able to fetch security logs", e);
        }
    }

    public static String getIdForNameByNamePatternProvider(String namePatternProviderId, String name) {

        TestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildIntegrationTestGetIdForNameByNamePatternProvider(namePatternProviderId, name);

        String result = IntegrationTestContext.get().getRestHelper(ANONYMOUS).getStringFromURL(url);
        return result;

    }

    /**
     * Changes a value inside scan config and wait until this value has been
     * reloaded
     */
    public static void waitForScanConfigRefresh() {
        LOG.debug("start wait for scan config refresh");
        String newValue = "" + System.nanoTime();
        MappingEntry entry = new MappingEntry("value", newValue, "just for integrationtest refresh");
        /* direct change necessary - to avoid filtering if this special entry */
        changeScanMappingDirectly(INTEGRATIONTEST_CHECK_SCANCONFIG_REFRESH_PROVIDERID, entry);

        String id = null;
        while (id == null || !(id.equals(newValue))) {
            LOG.info("Waiting for scan config refresh");
            waitMilliSeconds(1000);
            id = getIdForNameByNamePatternProvider(INTEGRATIONTEST_CHECK_SCANCONFIG_REFRESH_PROVIDERID, "value");
        }

    }

    public static void clearMetaDataInspection() {
        TestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildClearMetaDataInspectionURL();

        IntegrationTestContext.get().getSuperAdminRestHelper().delete(url);
    }

    public static List<Map<String, Object>> fetchMetaDataInspections() {
        TestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildFetchMetaDataInspectionsURL();

        String json = IntegrationTestContext.get().getSuperAdminRestHelper().getJSON(url);
        TestJSONHelper jsonHelper = TestJSONHelper.get();

        List<Map<String, Object>> data;
        try {
            data = jsonHelper.getMapper().readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
            return data;
        } catch (JsonProcessingException e) {
            throw new AssertionError("Was not able to read meta data json", e);
        }

    }

    /**
     * Will remove all waiting jobs in database + wait for all running jobs to be
     * done
     *
     */
    public static void ensureNoLongerJobExecution() {
        cancelAllScanJobs();
        removeAllJobsNotRunning();
        waitUntilNoLongerJobsRunning();
    }

    private static void removeAllJobsNotRunning() {
        LOG.debug("Start removing jobs not already running");

        String url = getURLBuilder().buildIntegrationTestDeleteAllWaitingJobsUrl();
        getSuperAdminRestHelper().delete(url);
    }

    /**
     * Waits until no longer running jobs are detected. will wait
     * #MAXIMUM_WAIT_FOR_RUNNING_JOBS milliseconds until time out
     */
    public static void waitUntilNoLongerJobsRunning() {
        LOG.debug("Start wait for no longer running jobs");
        String url = getURLBuilder().buildAdminFetchAllRunningJobsUrl();

        long timeOutInMilliseconds = MAXIMUM_WAIT_FOR_RUNNING_JOBS;

        long startTime = System.currentTimeMillis();
        int jobsFound = 1;
        while (jobsFound != 0) {
            try {
                long timeElapsed = System.currentTimeMillis() - startTime;
                if (timeElapsed > timeOutInMilliseconds) {
                    throw new IllegalStateException("Time out - even after " + timeElapsed + " ms we have still running jobs.");
                }
                String json = getSuperAdminRestHelper().getJSON(url);
                JsonNode obj = TestJSONHelper.get().getMapper().readTree(json);
                if (obj instanceof ArrayNode) {
                    ArrayNode an = (ArrayNode) obj;
                    jobsFound = an.size();
                    if (jobsFound == 0) {
                        return;
                    }
                }
                LOG.debug("- Jobs still running - so will wait and retry. Jobs found: {} {}", jobsFound, json);
                waitMilliSeconds(2000);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("JSON parsing failed!");
            }
        }
    }

    /**
     * Starts event inspection<br>
     * <br>
     *
     * To provide an empty event bus, without noise from other tests or still
     * running jobs, this method does following
     * <ul>
     * <li>cancel all scan jobs</li>
     * <li>remove all not already running jobs in scheduler</li>
     * <li>wait until no longer jobs are running</li>
     * <li>wait until no longer events are running</li>
     * <li>start event inspection</li>
     * </ul>
     * In some cases it can be necessary to start
     * TestAPI#waitUntilNoLongerRunningJobs() manual in tests - but only if before
     * calling this method the scheduling has been disabled...
     */
    public static void startEventInspection() {
        cancelAllScanJobs();

        ensureNoLongerJobExecution();
        /*
         * the initial reset will trigger also events (but fast) we wait until no longer
         * new events are flushed before doing the new inspection start
         */
        waitUntilNoLongerNewEventsTriggered(5, 300);

        String url = getURLBuilder().buildIntegrationTestStartEventInspection();
        getSuperAdminRestHelper().post(url);
    }

    /**
     * Cancels all running scan jobs - not only at scheduler!
     *
     * @return amount scan jobs
     */
    public static long cancelAllScanJobs() {
        String url = getURLBuilder().buildIntegrationTestCancelAllScanJobsUrl();
        return getSuperAdminRestHelper().getLongFromURL(url);
    }

    private static void waitUntilNoLongerNewEventsTriggered(int minLoopCount, int timeToWaitForNextCheckInMilliseconds) {
        /* we wait until we got no new events after dedicated time, so */
        int inspectionIdBefore = -1;
        int currentInspectionID = EventInspectionAPI.fetchLastInspectionId();

        long startTime = System.currentTimeMillis();

        int loop = 0;
        while (currentInspectionID != inspectionIdBefore && loop < minLoopCount) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > 120000) { // 120 seconds = two minutes max
                throw new IllegalStateException("Wait until no events failed - timeout reached:" + elapsedTime + " ms.");
            }
            loop++;
            LOG.debug("wait:{} ms, currentInspectionID:{}, inspectionIdBefore:{}", timeToWaitForNextCheckInMilliseconds, currentInspectionID,
                    inspectionIdBefore);
            inspectionIdBefore = currentInspectionID;

            waitMilliSeconds(timeToWaitForNextCheckInMilliseconds);
            currentInspectionID = EventInspectionAPI.fetchLastInspectionId();

            if (currentInspectionID != inspectionIdBefore) {
                loop = 0;// reset, so start mulitple time checks again
            }
        }
    }

    public static IntegrationTestEventHistory fetchEventInspectionHistory() {
        String url = getURLBuilder().buildIntegrationTestFetchEventInspectionHistory();
        String json = getSuperAdminRestHelper().getJSON(url);
        return IntegrationTestEventHistory.fromJSONString(json);
    }

    public static Map<String, String> listStatusEntries() {
        String url = getURLBuilder().buildAdminListsStatusEntries();
        String json = getSuperAdminRestHelper().getJSON(url);
        JsonNode node;
        try {
            node = TestJSONHelper.get().getMapper().readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot read json:" + json, e);
        }
        if (!node.isArray()) {
            throw new IllegalStateException("must be an array:" + json);
        }
        ArrayNode array = (ArrayNode) node;
        Map<String, String> map = new TreeMap<>();
        array.forEach(new Consumer<JsonNode>() {
            @Override
            public void accept(JsonNode node) {
                JsonNode key = node.get("key");
                JsonNode value = node.get("value");
                String keyText = key.textValue();
                String valueText = value.textValue();
                map.put(keyText, valueText);
            }
        });
        return map;
    }

    public static void refreshStatusEntries() {
        String url = getURLBuilder().buildAdminTriggersRefreshOfSchedulerStatus();
        getSuperAdminRestHelper().post(url);
    }

    private static IntegrationTestContext getContext() {
        IntegrationTestContext context = IntegrationTestContext.get();
        return context;
    }

    public static SortedMap<String, String> listSignups() {
        String url = getURLBuilder().buildAdminListsUserSignupsUrl();
        String json = getSuperAdminRestHelper().getJSON(url);
        JsonNode node;
        try {
            node = TestJSONHelper.get().getMapper().readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot read json:" + json, e);
        }
        if (!node.isArray()) {
            throw new IllegalStateException("must be an array:" + json);
        }
        ArrayNode array = (ArrayNode) node;
        SortedMap<String, String> map = new TreeMap<>();
        array.forEach(new Consumer<JsonNode>() {
            @Override
            public void accept(JsonNode node) {
                JsonNode userAsKey = node.get("userId");
                JsonNode emailAsValue = node.get("emailAdress");
                String keyText = userAsKey.textValue();
                String valueText = emailAsValue.textValue();
                map.put(keyText, valueText);
            }
        });
        return map;
    }

    private static TestRestHelper getSuperAdminRestHelper() {
        return getContext().getSuperAdminRestHelper();
    }

    private static TestURLBuilder getURLBuilder() {
        return getContext().getUrlBuilder();
    }

    private static TestURLBuilder getPDSURLBuilder() {
        return getContext().getPDSUrlBuilder();
    }

    public static void revertJobToStillRunning(UUID sechubJobUUID) {
        String url = getURLBuilder().buildIntegrationTestRevertJobAsStillRunning(sechubJobUUID);
        getSuperAdminRestHelper().put(url);
    }

    public static void revertJobToStillNotApproved(UUID sechubJobUUID) {
        String url = getURLBuilder().buildIntegrationTestRevertJobAsStillNotApproved(sechubJobUUID);
        getSuperAdminRestHelper().put(url);
    }

    public static void fakeProductResult(String projectId, UUID sechubJobUUID, String productId, String result) {
        String url = getURLBuilder().buildIntegrationTestFakeProductResult(projectId, sechubJobUUID, productId);
        getSuperAdminRestHelper().putPlainText(url, result);
    }

    public static long countJobResults(UUID sechubJobUUID) {
        String url = getURLBuilder().buildIntegrationTestCountProductResults(sechubJobUUID);
        return getSuperAdminRestHelper().getLongFromURL(url);
    }

    public static void destroyProductResults(UUID sechubJobUUID) {
        String url = getURLBuilder().buildintegrationTestDeleteProductResults(sechubJobUUID);
        getSuperAdminRestHelper().delete(url);
    }

    public static boolean isExecutionProfileExisting(String profileId) {
        String url = getURLBuilder().buildintegrationTestIsExecutionProfileExisting(profileId);
        return getSuperAdminRestHelper().getBooleanFromURL(url);
    }

    public static void dropExecutionProfileIfExisting(String profileId) {
        assertNoDefaultProfileId(profileId);
        if (!isExecutionProfileExisting(profileId)) {
            return;
        }
        String url = getURLBuilder().buildAdminDeletesProductExecutionProfile(profileId);
        getSuperAdminRestHelper().delete(url);
    }

    public static void assertNoDefaultProfileId(String profileId) {
        for (DefaultTestExecutionProfile doNotChangeProfile : IntegrationTestDefaultProfiles.getAllDefaultProfiles()) {
            if (doNotChangeProfile.id.equals(profileId)) {
                throw new IllegalArgumentException("Profile " + profileId
                        + " is a default profile and may not be changed! This would destroy test scenarios! Please define own profiles in your tests and change them!");
            }
        }
    }

    public static boolean canReloadExecutionProfileData(DefaultTestExecutionProfile profile) {
        if (!TestAPI.isExecutionProfileExisting(profile.id)) {
            return false;
        }
        reConnectStaticDataWithDatabaseContent(profile);
        return true;
    }

    private static void reConnectStaticDataWithDatabaseContent(TestExecutionProfile profile) {
        if (profile.configurations.isEmpty()) {
            LOG.info("reconnecting static data with existing database content of profiles");
            TestExecutionProfile profile2 = as(SUPER_ADMIN).fetchProductExecutionProfile(profile.id);

            profile.configurations.addAll(profile2.configurations);
            profile.enabled = profile2.enabled;
        }
        as(SUPER_ADMIN).ensureExecutorConfigUUIDs();
    }

    public static void switchSchedulerStrategy(String strategyId) {
        String url = getURLBuilder().buildSetSchedulerStrategyIdUrl(strategyId);
        getSuperAdminRestHelper().put(url);
    }

    /**
     * Waits for at least one heart beat by PDS server. Every 200 milliseconds there
     * is a check if at least one heart beat time stamp is found. After 10 tries the
     * method will fail.
     */
    public static void waitForAtLeastOnePDSHeartbeat() {
        int maxTries = 10;

        boolean heartBeatFound = false;
        int tried = 0;
        while (!heartBeatFound && tried < maxTries) {
            tried++;
            String json = asPDSUser(PDS_ADMIN).getMonitoringStatus();
            heartBeatFound = json.contains("heartBeatTimestamp");
            if (!heartBeatFound) {
                LOG.info("No heart beat time stamp found (tried {} times) - so will wait and retry", tried);
                waitMilliSeconds(200);
            }
        }
        assertTrue("Even after " + tried + " tries to fetch a heartbeat there was no heartbeat found!", heartBeatFound);

    }

    /**
     * Wait that project does not exist. Will try 9 times with 330 milliseconds
     * delay before next retry. After this time this method will fail.
     *
     * @param project
     */
    public static void waitProjectDoesNotExist(TestProject project) {
        assertProject(project).doesNotExist(9);
    }

}
