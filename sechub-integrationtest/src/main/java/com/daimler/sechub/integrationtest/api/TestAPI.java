// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

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
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;
import com.daimler.sechub.integrationtest.internal.TestJSONHelper;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistory;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestURLBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import junit.framework.AssertionFailedError;

public class TestAPI {

    private static final String INTEGRATIONTEST_CHECK_SCANCONFIG_REFRESH_PROVIDERID = "integrationtest.check.scanconfig.refresh.providerid";

    private static final Logger LOG = LoggerFactory.getLogger(TestAPI.class);

    /**
     * Do <b>NOT</b> change this user in tests! This is only for checks. Only
     * special scenario users are automatically reverted
     */
    public static final TestUser ANONYMOUS = new TestUser();

    /**
     * Do <b>NOT</b> change this user in tests! This is only for checks. Only
     * special scenario users are automatically reverted
     */
    public static final TestUser SUPER_ADMIN = new TestUser("int-test_superadmin", "int-test_superadmin-pwd",
            "superadmin@" + ExampleConstants.URI_SECHUB_SERVER);
    /**
     * Do <b>NOT</b> change this user in tests! This is only for checks. Only
     * special scenario users are automatically reverted
     */
    public static final TestUser ONLY_USER = new TestUser("int-test_onlyuser", "int-test_onlyuser-pwd", "onlyuser@" + ExampleConstants.URI_TARGET_SERVER);

    private static final long MAXIMUM_WAIT_FOR_RUNNING_JOBS = 300 * 1000;// 300 seconds = 5 minutes max;

    public static final AsUser as(TestUser user) {
        return new AsUser(user);
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

    public static AssertInspections assertInspections() {
        return new AssertInspections();
    }

    /**
     * Waits for sechub job being done - after 5 seconds time out is reached
     * 
     * @param project
     * @param jobUUID
     */
    @SuppressWarnings("unchecked")
    public static void waitForJobDone(TestProject project, UUID jobUUID) {
        LOG.debug("wait for job done project:{}, job:{}", project.getProjectId(), jobUUID);

        TestAPI.executeUntilSuccessOrTimeout(new AbstractTestExecutable(SUPER_ADMIN, 5, HttpClientErrorException.class) {
            @Override
            public boolean runImpl() throws Exception {
                String status = as(getUser()).getJobStatus(project.getProjectId(), jobUUID);
                System.out.println(">>>>>>>>>JOB:STATUS:" + status);
                return status.contains("OK");
            }
        });
    }

    private static boolean notExceeded(long maxMilliseconds, long start) {
        return System.currentTimeMillis() - start < maxMilliseconds;
    }

    public static void executeUntilSuccessOrTimeout(TestExecutable e) {
        long start = System.currentTimeMillis();
        int maxMilliseconds = e.getTimeoutInSeconds() * 1000;
        do {
            boolean stop = false;
            try {
                stop = e.run();
            } catch (Exception ex) {
                /* ignore */
                boolean handled = false;
                for (Class<? extends Exception> hec : e.getHandledExceptions()) {
                    if (ex.getClass().isAssignableFrom(hec)) {
                        handled = true;
                        break;
                    }
                }
                if (!handled) {
                    throw new IllegalStateException("An unexpected / unhandled exception occurred at execution time!", ex);
                }
            }
            if (stop) {
                return;
            }
            if (e.getTimeToWaitInMillis() > 0) {
                try {
                    Thread.sleep(e.getTimeToWaitInMillis());
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        } while (notExceeded(maxMilliseconds, start));
        fail("Timeout of waiting for successful execution - waited " + e.getTimeoutInSeconds() + " seconds");
        return;
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

        IntegrationTestContext.get().getRestHelper(ANONYMOUS).putJSon(url, data.toJSON());

    }

    public static MappingData fetchMappingDataDirectlyOrNull(String mappingId) {

        TestURLBuilder urlBuilder = IntegrationTestContext.get().getUrlBuilder();
        String url = urlBuilder.buildIntegrationTestFetchMappingDirectlyURL(mappingId);

        String result = IntegrationTestContext.get().getRestHelper(ANONYMOUS).getJSon(url);
        if (result == null) {
            return null;
        }
        MappingData data = MappingData.fromString(result);
        return data;

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

        String json = IntegrationTestContext.get().getSuperAdminRestHelper().getJSon(url);
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
     * Will remove all waiting jobs in database + wait for all running jobs to be done
     * 
     */
    public static void ensureNoLongerJobExecution() {
        removeAllJobsNotRunning();
        waitUntilNoLongerJobsRunning();
    }
    
    private static void removeAllJobsNotRunning() {
        LOG.debug("Start removing jobs not already running");
        
        IntegrationTestContext context = IntegrationTestContext.get();
        String url = context.getUrlBuilder().buildintegrationTestDeleteAllWaitingJobsUrl();
        context.getSuperAdminRestHelper().delete(url);
    }

    /**
     * Waits until no longer running jobs are detected. will wait #MAXIMUM_WAIT_FOR_RUNNING_JOBS milliseconds
     * until time out
     */
    public static void waitUntilNoLongerJobsRunning() {
        LOG.debug("Start wait for no longer running jobs");
        IntegrationTestContext context = IntegrationTestContext.get();
        String url = context.getUrlBuilder().buildAdminFetchAllRunningJobsUrl();

        long timeOutInMilliseconds = MAXIMUM_WAIT_FOR_RUNNING_JOBS;

        long startTime = System.currentTimeMillis();
        int jobsFound = 1;
        while (jobsFound != 0) {
            try {
                long timeElapsed = System.currentTimeMillis() - startTime;
                if (timeElapsed > timeOutInMilliseconds) {
                    throw new IllegalStateException("Time out - even after " + timeElapsed + " ms we have still running jobs.");
                }
                String json = context.getSuperAdminRestHelper().getJSon(url);
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
     * Will do:
     * <ul>
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
        ensureNoLongerJobExecution();
        /*
         * the initial reset will trigger also events (but fast) we wait until no longer
         * new events are flushed before doing the new inspection start
         */
        waitUntilNoLongerNewEventsTriggered(5, 300);

        IntegrationTestContext context = IntegrationTestContext.get();
        String url = context.getUrlBuilder().buildIntegrationTestStartEventInspection();
        context.getSuperAdminRestHelper().post(url);
    }

    private static void waitUntilNoLongerNewEventsTriggered(int minLoopCount, int timeToWaitForNextCheckInMilliseconds) {
        /* we wait until we got no new events after dedicated time, so */
        int inspectionIdBefore = -1;
        int currentInspectionID = EventInspectionAPI.fetchLastInspectionId();

        long startTime = System.currentTimeMillis();

        int loop = 0;
        while (currentInspectionID != inspectionIdBefore && loop < minLoopCount) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > 120.000) { // 120 seconds = two minutes max
                throw new IllegalStateException("Wait unttil no events failed - timeout reached:" + elapsedTime + " ms.");
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
        IntegrationTestContext context = IntegrationTestContext.get();
        String url = context.getUrlBuilder().buildIntegrationTestFetchEventInspectionHistory();
        String json = context.getSuperAdminRestHelper().getJSon(url);
        return IntegrationTestEventHistory.fromJSONString(json);
    }

    public static Map<String, String> listStatusEntries() {
        IntegrationTestContext context = IntegrationTestContext.get();
        String url = context.getUrlBuilder().buildAdminListsStatusEntries();
        String json = context.getSuperAdminRestHelper().getJSon(url);
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
        IntegrationTestContext context = IntegrationTestContext.get();
        String url = context.getUrlBuilder().buildAdminTriggersRefreshOfSchedulerStatus();
        context.getSuperAdminRestHelper().post(url);
    }

    public static SortedMap<String, String> listSignups() {
        IntegrationTestContext context = IntegrationTestContext.get();
        String url = context.getUrlBuilder().buildAdminListsUserSignupsUrl();
        String json = context.getSuperAdminRestHelper().getJSon(url);
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

}
