// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.domain.scan.admin.FullScanData;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.integrationtest.internal.DefaultTestExecutionProfile;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData.TestCleanupTimeUnit;
import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;
import com.mercedesbenz.sechub.integrationtest.internal.TestRestHelper;
import com.mercedesbenz.sechub.integrationtest.internal.autoclean.TestAutoCleanJsonDeleteCount;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogData;
import com.mercedesbenz.sechub.sharedkernel.messaging.IntegrationTestEventHistory;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.PDSTestURLBuilder;
import com.mercedesbenz.sechub.test.SecHubTestURLBuilder;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfile;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfig;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfigList;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfigListEntry;

import junit.framework.AssertionFailedError;

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

    private static File testReportStorageFolder = new File("./build/sechub-test-reports");

    private static TextFileWriter writer = new TextFileWriter();

    public static final AsUser as(TestUser user) {
        return new AsUser(user);
    }

    public static final AsPDSUser asPDSUser(TestUser user) {
        return new AsPDSUser(user);
    }

    public static AssertUserJobInfo assertUserJobInfo(TestSecHubJobInfoForUserListPage page) {
        return AssertUserJobInfo.assertInfo(page);
    }

    public static AssertEncryptionStatus assertEncryptionStatus() {
        return assertEncryptionStatus(as(SUPER_ADMIN).fetchEncryptionStatus());
    }

    public static AssertEncryptionStatus assertEncryptionStatus(SecHubEncryptionStatus status) {
        return AssertEncryptionStatus.assertEncryptionStatus(status);
    }

    /**
     * Asserts given report json - it will try to find report elements
     *
     * @param json
     * @return
     */
    public static AssertReportUnordered assertReportUnordered(String json) {
        return AssertReportUnordered.assertReportUnordered(json);
    }

    public static AssertExecutionResult assertExecutionResult(ExecutionResult result) {
        return AssertExecutionResult.assertResult(result);
    }

    /**
     * Asserts given report json - for checks you will always need to explicit use
     * indexes and the report must have an explicit ordering - otherwise you have
     * flaky tests!
     *
     * @param json
     * @return assert object
     */
    public static AssertReport assertReport(String json) {
        return AssertReport.assertReport(json);
    }

    /**
     * Asserts given report HTML (in memory)
     *
     * @param html string representation
     * @return assert object
     */
    public static AssertHTMLReport assertHTMLReport(String html) {
        return AssertHTMLReport.assertHTMLReport(html);
    }

    /**
     * Asserts given report HTML (from a file). When the html report has failures,
     * the failure text will provide the file path inside the failure output.
     *
     * @param html     string representation
     * @param filePath the file path where the HTML report comes from
     * @return assert object
     */
    public static AssertHTMLReport assertHTMLReport(String html, String filePath) {
        return AssertHTMLReport.assertHTMLReport(html, filePath);
    }

    public static AssertFullScanData assertFullScanDataZipFile(File file) {
        return AssertFullScanData.assertFullScanDataZipFile(file);
    }

    public static AssertPDSStatus assertPDSJobStatus(UUID pdsJobUUID) {
        String json = asPDSUser(PDS_ADMIN).getJobStatus(pdsJobUUID);
        return new AssertPDSStatus(json);
    }

    public static AssertPDSStatus assertPDSJobStatus(String json) {
        return new AssertPDSStatus(json);
    }

    public static AssertSecHubJobStatus assertJobStatus(TestProject project, UUID sechubJobUUID) {
        return new AssertSecHubJobStatus(sechubJobUUID, project);
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
     * @return assert object
     */
    public static AssertMetaDataInspections assertMetaDataInspections() {
        return new AssertMetaDataInspections();
    }

    /**
     * Creates an assert object to inspect auto cleanup data
     *
     * @return assert object
     */
    public static AssertAutoCleanupInspections assertAutoCleanupInspections() {
        return new AssertAutoCleanupInspections();
    }

    /**
     * Creates an assert object to inspect PDS jobs
     *
     * @return assert object
     */
    public static AssertPDSJob assertPDSJob(UUID pdsJobUUID) {
        return AssertPDSJob.assertPDSJob(pdsJobUUID);
    }

    public static AssertStatistic assertStatistic(UUID sechubJobUUID) {
        return AssertStatistic.assertStatistic(sechubJobUUID);
    }

    /**
     * Creates an assert object to inspect PDS auto cleanup data
     *
     * @return assert object
     */
    public static AssertPDSAutoCleanupInspections assertPDSAutoCleanupInspections() {
        return new AssertPDSAutoCleanupInspections();
    }

    public static void logInfoOnServer(String text) {
        String url = getURLBuilder().buildIntegrationTestLogInfoUrl();
        getContext().getRestHelper(ANONYMOUS).postPlainText(url, text);
    }

    public static void logInfoOnPDS(String text) {
        String url = getPDSURLBuilder().buildIntegrationTestLogInfoUrl();
        getContext().getPDSRestHelper(ANONYMOUS).postPlainText(url, text);
    }

    /**
     * When sechub storage is reused, this will return the storage path for the
     * sechub job uuid otherwise <code>null</code>
     *
     * @param secHubJobUUID
     * @return path or <code>null</code>
     */
    public static String fetchStoragePathHistoryEntryoForSecHubJobUUID(UUID secHubJobUUID) {
        String url = getPDSURLBuilder().buildIntegrationTestFetchStoragePathHistoryEntryForSecHubJob(secHubJobUUID);
        return getContext().getPDSRestHelper(ANONYMOUS).getStringFromURL(url);
    }

    /**
     * Resolve local PDS upload folder
     *
     * @param pdsJobUUID
     * @return upload folder in job workspace
     */
    public static File resolvePDSWorkspaceUploadFolder(UUID pdsJobUUID) {
        String url = getPDSURLBuilder().buildIntegrationTestGetWorkspaceUploadFolder(pdsJobUUID);
        String path = getContext().getPDSRestHelper(ANONYMOUS).getStringFromURL(url);
        return new File(path);

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
    public static void waitForJobDone(TestProject project, UUID jobUUID, int timeOutInSeconds, boolean jobMayNeverFail) {
        LOG.info("wait for job done project:{}, job:{}", project.getProjectId(), jobUUID);

        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, timeOutInSeconds, HttpClientErrorException.class) {

            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                TestSecHubJobStatus jobStatus = getSecHubJobStatus(project, jobUUID, getUser());

                if (jobMayNeverFail && jobStatus.hasResultFailed()) {
                    String prettyJSON = JSONConverter.get().toJSON(jobStatus, true);
                    fail("The job execution has failed - skip further attempts to check that job will be done.\n-Status data:\n" + prettyJSON
                            + "\n\n- Please refer to server and/or PDS logs for reason. You can search for the unit test method name inside these logs.");
                }
                return jobStatus.hasResultOK();
            }

        });
    }

    public static TestSecHubJobStatus getSecHubJobStatus(TestProject project, UUID jobUUID, TestUser asUser) {
        String status = as(asUser).getJobStatus(project.getProjectId(), jobUUID);
        LOG.info(" => Job status: {}", status);
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

    public static UUID waitForPDSJobOfSecHubJobAtGivenPositionAndReturnPDSJobUUID(UUID sechubJobUUID, int index) {
        String indexNotFoundErrorMessage = "Did not found PDS job [" + index + "] uuid was found for sechub job:" + sechubJobUUID;
        return executeCallableAndAcceptAssertionsMaximumTimes(15, () -> {

            List<UUID> allPDSJobUUIDs = TestAPI.fetchAllPDSJobUUIDsForSecHubJob(sechubJobUUID);
            assertTrue(indexNotFoundErrorMessage, allPDSJobUUIDs.size() > index);
            return allPDSJobUUIDs.get(index);
        }, 1000);
    }

    public static UUID waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(UUID sechubJobUUID) {
        return waitForPDSJobOfSecHubJobAtGivenPositionAndReturnPDSJobUUID(sechubJobUUID, 0);
    }

    public static void waitForPDSJobInState(PDSJobStatusState wantedState, int timeOutInSeconds, int timeToWaitInMillis, UUID pdsJobUUID,
            boolean dumpPDSOutputOnTimeOut) {
        Runnable runnable = null;
        if (dumpPDSOutputOnTimeOut) {
            runnable = new AutoDumpPDSOutputForPDSJobUUIDRunnable(pdsJobUUID);
        }
        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, timeOutInSeconds, timeToWaitInMillis, runnable, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                String status = asPDSUser(PDS_ADMIN).getJobStatus(pdsJobUUID);
                LOG.debug(">>>>>>>>>PDS JOB:STATUS:" + status);
                boolean wantedStateFound = status.contains(wantedState.toString());

                if (wantedState != PDSJobStatusState.FAILED) {
                    boolean statusIsFailed = status.contains(PDSJobStatusState.FAILED.toString());
                    if (statusIsFailed) {
                        /* it has failed and failed is not expected - so this is a problem! */
                        String outputStreamText = asPDSUser(PDS_ADMIN).getJobOutputStreamText(pdsJobUUID);
                        String errorStreamText = asPDSUser(PDS_ADMIN).getJobErrorStreamText(pdsJobUUID);

                        String message = """
                                PDS job: %s status not as expected
                                - actual: %s
                                - expected: %s

                                Output stream:
                                ----------------------
                                %s

                                Error stream:
                                ----------------------
                                %s

                                """.formatted(pdsJobUUID, status, wantedState, outputStreamText, errorStreamText);

                        fail(message);

                    }
                }
                return wantedStateFound;
            }
        });

    }

    /**
     * Wait until SecHub job is running
     *
     * @param project
     * @param timeOutInSeconds
     * @param timeToWaitInMillis
     * @param jobUUID
     */
    public static void waitForJobRunning(TestProject project, int timeOutInSeconds, int timeToWaitInMillis, UUID jobUUID) {
        LOG.info("wait for job running project:{}, job:{}, timeToWaitInMillis{}, timeOutInSeconds:{}", project.getProjectId(), jobUUID, timeToWaitInMillis,
                timeOutInSeconds);

        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, timeOutInSeconds, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                return containsStatus(getUser(), project, jobUUID, "STARTED");
            }
        });
    }

    /**
     * Waits for sechub job cancel requested - after 5 seconds time out is reached
     *
     * @param project
     * @param jobUUID
     */
    public static void waitForJobStatusCancelRequestedOrCanceled(TestProject project, UUID jobUUID) {
        LOG.info("wait for job status is 'cancel requested' or 'canceled'. project:{}, job:{}", project.getProjectId(), jobUUID);

        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, 5, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                return containsStatus(getUser(), project, jobUUID, "CANCEL_REQUESTED", "CANCELED");
            }
        });
    }

    private static boolean containsStatus(TestUser user, TestProject project, UUID jobUUID, String... acceptedContainedStatus) {
        String status = as(user).getJobStatus(project.getProjectId(), jobUUID);
        LOG.info(" => Job status: {}", status);
        for (String accepted : acceptedContainedStatus) {
            if (status.contains(accepted)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Waits for sechub job being finally canceled - after 5 seconds time out is
     * reached
     *
     * @param project
     * @param jobUUID
     */
    public static void waitForJobStatusCanceled(TestProject project, UUID jobUUID, boolean dumpPDSOutputOnTimeOut) {
        LOG.info("wait for job status is 'canceled'. project:{}, job:{}", project.getProjectId(), jobUUID);
        Runnable runnable = null;
        if (dumpPDSOutputOnTimeOut) {
            runnable = new AutoDumpPDSOutputForSecHubJobUUIDRunnable(jobUUID);
        }

        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, 5, runnable, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                return containsStatus(getUser(), project, jobUUID, "CANCELED");
            }
        });
    }

    /**
     * Waits for sechub job being in state SUSPENDED - after 5 seconds time out is
     * reached
     *
     * @param project
     * @param jobUUID
     */
    public static void waitForJobStatusSuspended(TestProject project, UUID jobUUID) {
        LOG.info("wait for job status is 'suspended'. project:{}, job:{}", project.getProjectId(), jobUUID);
        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, 5, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                return containsStatus(getUser(), project, jobUUID, "SUSPENDED");
            }
        });
    }

    /**
     * Waits for sechub job being failed - after 5 seconds time out is reached
     *
     * @param project
     * @param jobUUID
     */
    public static void waitForJobStatusFailed(TestProject project, UUID jobUUID) {
        LOG.info("wait for job failed project:{}, job:{}", project.getProjectId(), jobUUID);

        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, 5, HttpClientErrorException.class) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                return containsStatus(getUser(), project, jobUUID, "FAILED");
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
                    throw new IllegalStateException("An unexpected / unhandled exception occurred at execution time:\n" + exception.getClass(), exception);
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

        Runnable timeOutRunnable = testExecutable.getTimeOutRunnable();
        if (timeOutRunnable != null) {
            try {
                timeOutRunnable.run();
            } catch (RuntimeException e) {
                System.err.println("Problem in test framework happend:");
                e.printStackTrace();
            }
        }
        /* was not possible to execute succesful in given time range */
        fail("Timeout of waiting for successful execution - waited " + testExecutable.getTimeoutInSeconds() + " seconds");
        return;
    }

    /**
     * Tries to execute runnable with default maximum time and retry (4 times a 500
     * milliseconds) Shortcut for
     * <code>executeRunnableAndAcceptAssertionsMaximumTimes(4,runnable, 500);</code>
     *
     * @param runnable
     */
    public static void executeResilient(Runnable runnable) {
        executeRunnableAndAcceptAssertionsMaximumTimes(4, runnable, 500);
    }

    public static void executeRunnableAndAcceptAssertionsMaximumTimes(int tries, Runnable runnable, int millisBeforeNextRetry) {
        executeCallableAndAcceptAssertionsMaximumTimes(tries, () -> {
            runnable.run();
            return null;
        }, millisBeforeNextRetry);
    }

    public static <T> T executeCallableAndAcceptAssertionsMaximumTimes(int tries, Callable<T> assertionCallable, int millisBeforeNextRetry) {
        T result = null;
        AssertionError assertionError = null;
        for (int i = 0; i < tries; i++) {
            /* reset error */
            assertionError = null;
            try {
                if (i > 0) {
                    /* we wait before next check */
                    TestAPI.waitMilliSeconds(millisBeforeNextRetry);
                }
                result = assertionCallable.call();
            } catch (AssertionError e) {
                assertionError = e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (assertionError == null) {
                break;
            }
            LOG.info("Will try again in {} ms, will try again {} times of {}", millisBeforeNextRetry, tries - i, tries);
        }
        if (assertionError != null) {
            throw assertionError;
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

    public static void updateEmailByOneTimeTokenLink(URI uri) {
        as(ANONYMOUS).sendGetRequestToURI(uri);
        // as(ANONYMOUS).getStringFromURL(uri.toString());
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
        return getLinkFromMail(text);
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
        return getLinkFromMail(text);
    }

    private static String getLinkFromMail(String text) {
        String[] lines = text.split("\n");

        String linkOfOneApiToken = lines[lines.length - 1];
        if (linkOfOneApiToken.isEmpty()) {
            fail("empty link line, origin text mail was:\n" + text);
        }
        return linkOfOneApiToken;
    }

    public static String getLinktToVerifyEmailAddressAfterChangeRequest(String newMailAddress, TestUser user) {
        LOG.debug("Get link to verify email address after change requested for user:{}", user.getUserId());
        MockEmailEntry mail = IntegrationTestContext.get().emailAccess().findMailOrFail(newMailAddress, "Verify new SecHub account email address");
        String text = mail.text.trim();
        String linkOfOneApiToken = getLinkFromMail(text);
        return linkOfOneApiToken;
    }

    /**
     * Expects an HTTP failure when runnable is executed. If this does not happen,
     * dedicated error messages comes up and unit test will fail.
     *
     * @param expectedStatusCode
     * @param runnable
     */
    public static void expectHttpFailure(Runnable runnable, HttpStatus expectedStatusCode) {
        internalExpectHttpFailure(runnable, -1, expectedStatusCode, null);
    }

    /**
     * Expects an HTTP failure when runnable is executed. If this does not happen,
     * dedicated error messages comes up and unit test will fail.
     *
     * @param runnable
     * @param exceptionValidator if set the exception will be inspected in detail,
     *                           if <code>null</code> no additional inspection
     */
    public static void expectHttpFailure(Runnable runnable, HttpStatusCodeExceptionTestValidator exceptionValidator) {
        internalExpectHttpFailure(runnable, -1, null, exceptionValidator);
    }

    /**
     * Expects an HTTP failure when runnable is executed. If this does not happen,
     * dedicated error messages comes up and unit test will fail.
     *
     * @param runnable
     * @param exceptionValidator    if set the exception will be inspected in
     *                              detail, if <code>null</code> no additional
     *                              inspection
     * @param timeOutInMilliseconds as long this time out is not reached HTTP 200
     *                              messages will be ignored and after a short break
     *                              the runnable will be called again to provoke
     *                              expected failure.
     */
    public static void expectHttpFailure(Runnable runnable, long timeOutInMilliseconds, HttpStatusCodeExceptionTestValidator exceptionValidator) {
        internalExpectHttpFailure(runnable, timeOutInMilliseconds, null, exceptionValidator);
    }

    /**
     * Expects an HTTP failure when runnable is executed. If this does not happen,
     * dedicated error messages comes up and unit test will fail.
     *
     * @param expectedStatusCode
     * @param timeOutInMilliseconds as long this time out is not reached HTTP 200
     *                              messages will be ignored and after a short break
     *                              the runnable will be called again to provoke
     *                              expected failure.
     * @param runnable
     */
    public static void expectHttpFailure(Runnable runnable, long timeOutInMilliseconds, HttpStatus expectedStatusCode) {
        internalExpectHttpFailure(runnable, timeOutInMilliseconds, expectedStatusCode, null);
    }

    private static void internalExpectHttpFailure(Runnable runnable, long timeOutInMilliseconds, HttpStatus expectedStatusCode,
            HttpStatusCodeExceptionTestValidator exceptionValidator) {
        if (exceptionValidator == null) {
            if (expectedStatusCode == null) {
                throw new IllegalArgumentException("test case corrupt please add at least one expected error!");
            }
            /*
             * when explicit exception validator set, we always check for Json server error
             * fields and expected status code
             */
            exceptionValidator = new JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(expectedStatusCode);
        }

        long start = System.currentTimeMillis();
        boolean timeElapsed = false;
        while (!timeElapsed) { /* NOSONAR */
            long waitedTimeInMilliseconds = System.currentTimeMillis() - start;
            timeElapsed = waitedTimeInMilliseconds > timeOutInMilliseconds;

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
                if (exceptionValidator != null) {
                    exceptionValidator.validate(he);
                }

            } catch (RestClientException e) {
                fail("Expected a " + HttpStatusCodeException.class.getSimpleName() + " but was " + e.getClass());
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
        SecHubTestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildGetFileUpload(project.getProjectId(), jobUUID.toString(), fileName);
        try {
            File file = as(ANONYMOUS).downloadAsTempFileFromURL(url, jobUUID, fileName);
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
        SecHubTestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildIntegrationTestChangeMappingDirectlyURL(mappingId);

        IntegrationTestContext.get().getRestHelper(ANONYMOUS).putJSON(url, data.toJSON());

    }

    public static MappingData fetchMappingDataDirectlyOrNull(String mappingId) {

        SecHubTestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildIntegrationTestFetchMappingDirectlyURL(mappingId);

        String result = IntegrationTestContext.get().getRestHelper(ANONYMOUS).getJSON(url);
        if (result == null) {
            return null;
        }
        MappingData data = MappingData.fromString(result);
        return data;

    }

    public static void clearSecurityLogs() {
        SecHubTestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildIntegrationTestClearSecurityLogs();

        IntegrationTestContext.get().getRestHelper(ANONYMOUS).delete(url);
    }

    public static List<SecurityLogData> getSecurityLogs() {
        SecHubTestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
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

        SecHubTestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
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
        SecHubTestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildClearMetaDataInspectionURL();

        IntegrationTestContext.get().getSuperAdminRestHelper().delete(url);
    }

    public static List<Map<String, Object>> fetchMetaDataInspections() {
        SecHubTestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
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
                    throw new CriticalTestProblemException("Time out - even after " + timeElapsed + " ms we have still running jobs.");
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
                JsonNode emailAsValue = node.get("emailAddress");
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

    private static TestRestHelper getPDSAdminRestHelper() {
        return getContext().getPDSRestHelper(PDS_ADMIN);
    }

    private static SecHubTestURLBuilder getURLBuilder() {
        return getContext().getUrlBuilder();
    }

    private static PDSTestURLBuilder getPDSURLBuilder() {
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
            LOG.info("reconnect static data with existing database content of profile: {}", profile.id);

            TestExecutionProfile profile2 = as(SUPER_ADMIN).fetchProductExecutionProfile(profile.id);

            profile.configurations.addAll(profile2.configurations);
            profile.enabled = profile2.enabled;
        }
        ensureExecutorConfigUUIDs(profile);
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

    private static void waitUntilScheduleAutoCleanupInDays(long days) {
        String url = getURLBuilder().buildIntegrationTestFetchScheduleAutoCleanupDaysUrl();
        waitUntilAutoCleanupInDays(days, url);
    }

    private static void waitUntilScanAutoCleanupInDays(long days) {
        String url = getURLBuilder().buildIntegrationTestFetchScanAutoCleanupDaysUrl();
        waitUntilAutoCleanupInDays(days, url);
    }

    private static void waitUntilAdministrationAutoCleanupInDays(long days) {
        String url = getURLBuilder().buildIntegrationTestFetchScanAutoCleanupDaysUrl();
        waitUntilAutoCleanupInDays(days, url);
    }

    private static void waitUntilAutoCleanupInDays(long days, String url) {
        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, 2, 200) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                long foundDays = getSuperAdminRestHelper().getLongFromURL(url);
                return foundDays == days;
            }
        });
    }

    /**
     * Starts cipher pool cleanup for scheduler domain directly for test scenario.
     * Normally this is done by auto cleanup mechanism only, but with this method it
     * is also possible to trigger the cleanup inside integration tests.
     */
    public static void startScheduleCipherPoolDataCleanup() {
        resetAutoCleanupDays(0);

        String url = getURLBuilder().buildIntegrationTestStartScheduleCipherPoolDataCleanup();
        getSuperAdminRestHelper().put(url);
    }

    /**
     * Will ensure complete auto cleanup inspector is reset and that auto cleanup is
     * set to "wantedFormerDays days" in configuration and also in every domain auto
     * clean day value.
     */
    public static void resetAutoCleanupDays(int wantedFormerDays) {
        ensureAutoCleanupSetToDays(wantedFormerDays);
        resetIntegrationTestAutoCleanupInspectorEvents();
    }

    public static void resetPDSAutoCleanupDaysToZero() {
        TestAutoCleanupData data = new TestAutoCleanupData(0, TestCleanupTimeUnit.DAY);
        asPDSUser(PDS_ADMIN).updateAutoCleanupConfiguration(data);
        waitUntilPDSAutoCleanupConfigurationChangedTo(data);
    }

    public static void resetIntegrationTestAutoCleanupInspectorEvents() {
        String url = getURLBuilder().buildIntegrationTestResetAutoCleanupInspectionUrl();
        getSuperAdminRestHelper().post(url);
    }

    public static void resetPDSIntegrationTestAutoCleanupInspector() {
        String url = getPDSURLBuilder().buildIntegrationTestResetAutoCleanupInspectionUrl();
        getPDSAdminRestHelper().post(url);
    }

    /**
     * Will ensure auto cleanup configuration is set to given days and that every
     * domain is synched.
     */
    public static void ensureAutoCleanupSetToDays(int days) {
        TestAutoCleanupData wanted = new TestAutoCleanupData(days, TestCleanupTimeUnit.DAY);
        TestAutoCleanupData autoCleanupConfig = as(SUPER_ADMIN).fetchAutoCleanupConfiguration();

        if (!wanted.equals(autoCleanupConfig)) {
            /* turn off by setting to 0 days */
            TestAutoCleanupData data = new TestAutoCleanupData(days, TestCleanupTimeUnit.DAY);

            as(SUPER_ADMIN).updateAutoCleanupConfiguration(data);

            waitUntilAutoCleanupConfigurationChangedTo(data);
        }
        waitUntilEveryDomainHasAutoCleanupSynchedToDays(days);
    }

    public static void waitUntilAutoCleanupConfigurationChangedTo(TestAutoCleanupData data) {
        executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, 2, 200) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                TestAutoCleanupData autoCleanupConfig2 = as(SUPER_ADMIN).fetchAutoCleanupConfiguration();
                return data.equals(autoCleanupConfig2);
            }
        });
    }

    public static void waitUntilPDSAutoCleanupConfigurationChangedTo(TestAutoCleanupData data) {
        executeUntilSuccessOrTimeout(new AbstractTestExecutable(PDS_ADMIN, 2, 200) {
            @Override
            public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                TestAutoCleanupData autoCleanupConfig2 = asPDSUser(PDS_ADMIN).fetchAutoCleanupConfiguration();
                return data.equals(autoCleanupConfig2);
            }
        });
    }

    public static void waitUntilEveryDomainHasAutoCleanupSynchedToDays(long days) {
        waitUntilScheduleAutoCleanupInDays(days);
        waitUntilScanAutoCleanupInDays(days);
        waitUntilAdministrationAutoCleanupInDays(days);
    }

    public static List<TestAutoCleanJsonDeleteCount> fetchPDSAutoCleanupInspectionDeleteCounts() {
        String url = getPDSURLBuilder().buildIntegrationTestFetchAutoCleanupInspectionDeleteCountsUrl();
        String json = getPDSAdminRestHelper().getJSON(url);
        return convertAutoCleanJson(json);
    }

    public static List<TestAutoCleanJsonDeleteCount> fetchAutoCleanupInspectionDeleteCounts() {
        String url = getURLBuilder().buildIntegrationTestFetchAutoCleanupInspectionDeleteCountsUrl();
        String json = getSuperAdminRestHelper().getJSON(url);
        return convertAutoCleanJson(json);

    }

    public static Long fetchScheduleEncryptionPoolIdForJob(UUID jobUUID) {
        String url = getURLBuilder().buildIntegrationTestFetchScheduleEncryptionPoolIdForSecHubJob(jobUUID);
        String result = getSuperAdminRestHelper().getStringFromURL(url);
        return Long.valueOf(result);
    }

    public static FullScanData fetchFullScanData(UUID sechubJobUIUD) {

        String url = getURLBuilder().buildIntegrationTestFetchFullScandata(sechubJobUIUD);
        String json = getSuperAdminRestHelper().getJSON(url);
        System.out.println(json);
        return JSONConverter.get().fromJSON(FullScanData.class, json);
    }

    private static List<TestAutoCleanJsonDeleteCount> convertAutoCleanJson(String json) {
        MappingIterator<TestAutoCleanJsonDeleteCount> result = TestJSONHelper.get().createValuesFromJSON(json, TestAutoCleanJsonDeleteCount.class);
        try {
            return result.readAll();
        } catch (IOException e) {
            throw new IllegalStateException("Was not able to inspect test data", e);
        }
    }

    public static UUID assertAndFetchPDSJobUUIDForSecHubJob(UUID sechubJobUUID) {
        List<UUID> pdsJobUUIDs = fetchAllPDSJobUUIDsForSecHubJob(sechubJobUUID);
        assertEquals("Must find one jobUUID", 1, pdsJobUUIDs.size());

        UUID pdsJobUUID = pdsJobUUIDs.iterator().next();
        return pdsJobUUID;
    }

    public static List<UUID> fetchAllPDSJobUUIDsForSecHubJob(UUID sechubJobUUID) {

        String url = getURLBuilder().buildIntegrationtTestFetchAllPDSJobUUIDSForSecHubJob(sechubJobUUID);
        String json = getSuperAdminRestHelper().getJSON(url);
        List<String> found = TestJSONHelper.get().createFromJSONAsList(json, String.class);
        List<UUID> jobUUIDS = found.stream().map((string) -> UUID.fromString(string)).collect(Collectors.toList());
        LOG.info("Found PDS job uuids:{} for sechub job:{}", jobUUIDS, sechubJobUUID);
        return jobUUIDS;
    }

    public static String getPDSServerEnvironmentVariableValue(String environmentVariableName) {
        String url = getPDSURLBuilder().buildIntegrationTestFetchEnvironmentVariableValue(environmentVariableName);
        String value = getPDSAdminRestHelper().getStringFromURL(url);
        return value;
    }

    public static void dumpAllPDSJobOutputsForSecHubJob(UUID sechubJobUUID) {
        dumpAllPDSJobOutputsForSecHubJob(sechubJobUUID, null);
    }

    public static void dumpAllPDSJobOutputsForSecHubJob(UUID sechubJobUUID, TestOutputOptions options) {
        System.out.println("##########################################################################################################");
        System.out.println("# DUMP all PDS Jobs for SecHub job: " + sechubJobUUID);
        System.out.println("##########################################################################################################");

        List<UUID> pdsJobUUIDs = internalExecuteOrUseFallback(() -> fetchAllPDSJobUUIDsForSecHubJob(sechubJobUUID), new ArrayList<>());
        for (UUID pdsJobUUID : pdsJobUUIDs) {
            dumpPDSJobOutput(pdsJobUUID, options);
        }
    }

    public static void dumpPDSJobOutput(UUID jobUUID) {
        dumpPDSJobOutput(jobUUID, null);
    }

    public static void dumpPDSJobOutput(UUID jobUUID, TestOutputOptions options) {
        if (options == null) {
            options = TestOutputOptions.create();
        }

        AsPDSUser asPDSUser = asPDSUser(PDS_ADMIN);

        String outputStreamText = internalExecuteOrUseFallback(() -> asPDSUser.internalFetchOutputStreamTextWithoutAutoDump(jobUUID),
                "cannot fetch output stream");
        String errorStreamText = internalExecuteOrUseFallback(() -> asPDSUser.internalFetchErrorStreamTextWithoutAutoDump(jobUUID),
                "cannot fetch error stream");

        SecHubMessagesList messagesList = internalExecuteOrUseFallback(() -> asPDSUser.internalGetJobMessagesWithoutAutoDump(jobUUID), null);
        String messagesAsString = null;
        if (messagesList != null) {
            messagesAsString = internalExecuteOrUseFallback(() -> JSONConverter.get().toJSON(messagesList, true), null);
        }
        if (messagesAsString == null) {
            messagesAsString = "MessagesList was null";
        }

        String report = internalExecuteOrUseFallback(() -> asPDSUser.internalFetchReportWithoutAutoDump(jobUUID, 1), ">>>>>>No report avialable<<<<<<");

        Object status = internalExecuteOrUseFallback(() -> asPDSUser.internalFetchStatusWithoutAutoDump(jobUUID), ">>>>>>No status avialable<<<<<<");

        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println("DUMP - PDS Job: " + jobUUID);
        System.out.println("----------------------------------------------------------------------------------------------------------");
        if (options.isWithStatus()) {
            System.out.println("Status:");
            System.out.println(status);
            System.out.println();
        }
        if (options.isWithOutput()) {
            System.out.println("Output stream:");
            System.out.println("--------------");
            if (outputStreamText == null || outputStreamText.isEmpty()) {
                System.out.println(">>>>>>No output avialable<<<<<<");
            } else {
                System.out.println(outputStreamText);
            }
            System.out.println();
        }
        if (options.isWithError()) {
            System.out.println("Error stream:");
            System.out.println("-------------");
            if (errorStreamText == null || errorStreamText.isEmpty()) {
                System.out.println(">>>>>>No error output avialable<<<<<<");
            } else {
                System.out.println(errorStreamText);
            }
            System.out.println();
        }
        if (options.isWithMessages()) {
            System.out.println("Messages:");
            System.out.println("---------");
            if (messagesAsString == null || messagesAsString.isEmpty()) {
                System.out.println(">>>>>>No messages avialable<<<<<<");
            } else {
                System.out.println(messagesAsString);
            }
            System.out.println();
        }
        if (options.isWithReport()) {
            System.out.println("Report:");
            System.out.println("-------");
            System.out.println(report);
            System.out.println();
        }
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println("END OF DUMP - PDS Job: " + jobUUID);
        System.out.println("----------------------------------------------------------------------------------------------------------");

    }

    private static <T> T internalExecuteOrUseFallback(Callable<T> callable, T fallback) {
        try {
            return callable.call();
        } catch (Exception e) {
            System.out.println(">> Internal execute failed. Fallback (" + fallback + ") necessary, because of :" + e.getMessage());
            return fallback;
        }
    }

    public static String createPDSJobFor(UUID sechubJobUUID, Map<String, String> params, String productId, TestRestHelper restHelper,
            PDSTestURLBuilder urlBuilder) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"apiVersion\":\"1.0\",\"sechubJobUUID\":\"").append(sechubJobUUID.toString()).append("\",\"productId\":\"").append(productId)
                .append("\",");
        sb.append("\"parameters\":[");

        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            sb.append("{\"key\":\"").append(key).append("\",");
            sb.append("\"value\":\"").append(params.get(key)).append("\"}");
            if (it.hasNext()) {
                sb.append(',');
            }
        }
        sb.append("]}}");

        String jobConfigurationJson = sb.toString();

        return createPDSJob(restHelper, urlBuilder, jobConfigurationJson);
    }

    public static String createPDSJob(TestRestHelper restHelper, PDSTestURLBuilder urlBuilder, String jobConfigurationJson) {
        String url = urlBuilder.buildCreateJob();
        String result = restHelper.postJson(url, jobConfigurationJson);
        return result;
    }

    public static Map<String, String> fetchPDSVariableTestOutputMap(UUID pdsJobUUID) {
        Map<String, String> result = new LinkedHashMap<>();

        String outputStreamText = asPDSUser(PDS_ADMIN).getJobOutputStreamText(pdsJobUUID);
        assertNotNull(outputStreamText);

        String[] lines = outputStreamText.split("\n");
        for (String line : lines) {
            if (line.startsWith(">") && line.length() > 1) {
                String shrinked = line.substring(1);
                String[] splitted = shrinked.split("=");
                String variableName = "";
                String variableValue = "";
                if (splitted.length > 0) {
                    variableName = splitted[0];
                }
                if (splitted.length > 1) {
                    variableValue = splitted[1];
                }
                result.put(variableName, variableValue);
            }
        }
        return result;
    }

    public static TestJobStatistic fetchJobStatistic(UUID sechubJobUUID) {
        String url = getURLBuilder().buildIntegrationTestFetchJobStatistic(sechubJobUUID);
        String json = getSuperAdminRestHelper().getJSON(url);

        return JSONConverter.get().fromJSON(TestJobStatistic.class, json);
    }

    public static List<TestJobStatisticData> fetchJobStatisticData(UUID sechubJobUUID) {
        String url = getURLBuilder().buildIntegrationTestFetchJobStatisticData(sechubJobUUID);
        String json = getSuperAdminRestHelper().getJSON(url);

        return JSONConverter.get().fromJSONtoListOf(TestJobStatisticData.class, json);
    }

    public static List<TestJobRunStatistic> fetchJobRunStatisticListForSecHubJob(UUID sechubJobUUID) {
        String url = getURLBuilder().buildIntegrationTestFetchJobRunStatistic(sechubJobUUID);
        String json = getSuperAdminRestHelper().getJSON(url);

        return JSONConverter.get().fromJSONtoListOf(TestJobRunStatistic.class, json);
    }

    public static List<TestJobRunStatisticData> fetchJobRunStatisticData(UUID executionUUID) {
        String url = getURLBuilder().buildIntegrationTestFetchJobRunStatisticData(executionUUID);
        String json = getSuperAdminRestHelper().getJSON(url);

        return JSONConverter.get().fromJSONtoListOf(TestJobRunStatisticData.class, json);
    }

    /**
     * Stores given report data inside "build/sechub-test-reports"
     *
     * @param fileName   the report file name to use for storage
     * @param reportData the report data to store
     */
    public static void storeTestReport(String fileName, String reportData) {
        try {
            writer.writeTextToFile(new File(testReportStorageFolder, fileName), reportData, true);
        } catch (Exception e) {
            LOG.error("Was not able to store sechub test report: {}", fileName, e);
        }
    }

    public static void ensureExecutorConfigUUIDs(TestExecutionProfile profile) {
        boolean atLeastOneWithoutUUID = false;
        for (TestExecutorConfig config : profile.configurations) {

            if (config.uuid == null) {
                atLeastOneWithoutUUID = true;
                break;
            }
        }
        if (!atLeastOneWithoutUUID) {
            return;
        }
        /* reload necessary */
        LOG.debug("At least one executor config for profile: {}: has no uuid, seems to be a local integration test restart. Start reconnecting.", profile.id);
        TestExecutorConfigList executorConfigList = as(SUPER_ADMIN).fetchProductExecutorConfigList(); // fetch only one time

        for (TestExecutorConfig config : profile.configurations) {
            ensureConfigHasUUID(config, executorConfigList);
        }
    }

    public static UUID ensureExecutorConfigUUID(TestExecutorConfig executorConfig) {
        return ensureConfigHasUUID(executorConfig, null);
    }

    private static UUID ensureConfigHasUUID(TestExecutorConfig executorConfig, TestExecutorConfigList executorConfigList) {
        if (executorConfig.uuid != null) {
            return executorConfig.uuid;
        }
        if (executorConfigList == null) {
            LOG.info("Load executor config list from database for executorConfig: {}", executorConfig.name);
            executorConfigList = as(SUPER_ADMIN).fetchProductExecutorConfigList(); // fetch only one time
        }

        LOG.info("searching executor config with name: {}", executorConfig.name);
        for (TestExecutorConfigListEntry entry : executorConfigList.executorConfigurations) {

            LOG.info("- found executor config with name: {}", entry.name);

            if (executorConfig.name.equals(entry.name)) {
                executorConfig.uuid = entry.uuid;
                LOG.info("- accepted for reconnect");
                break;
            }
        }
        if (executorConfig.uuid == null) {
            LOG.error("Loaded executor config list does not contain {}:\n{}", executorConfig.name, JSONConverter.get().toJSON(executorConfigList, true));

            throw new IllegalStateException("Reconnection of executor config failed! config name: " + executorConfig.name);
        }
        return executorConfig.uuid;
    }

    public static void triggerSecHubTerminationService() {
        String url = getURLBuilder().buildIntegrationTestChangeTerminationState(true);
        getSuperAdminRestHelper().put(url);
    }

    public static void resetSecHubTerminationService() {
        String url = getURLBuilder().buildIntegrationTestChangeTerminationState(false);
        getSuperAdminRestHelper().put(url);
    }

    public static boolean isSecHubTerminating() {
        String url = getURLBuilder().buildIntegrationTestFetchTerminationState();
        return getSuperAdminRestHelper().getBooleanFromURL(url);
    }

    public static List<ScanProjectConfig> fetchScanProjectConfigurations(TestProject project) {
        String url = getURLBuilder().buildIntegrationTestFetchScanProjectConfigurations(project.getProjectId());
        String json = getSuperAdminRestHelper().getJSON(url);
        return JSONConverter.get().fromJSONtoListOf(ScanProjectConfig.class, json);

    }

    public static void clearAllExistingTemplates() {
        String url = getURLBuilder().buildIntegrationTestClearAllTemplates();
        getSuperAdminRestHelper().post(url);

    }
}
