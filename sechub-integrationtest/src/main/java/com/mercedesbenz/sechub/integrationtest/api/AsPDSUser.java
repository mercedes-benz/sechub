// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.mercedesbenz.sechub.integrationtest.internal.TestRestHelper;
import com.mercedesbenz.sechub.test.TestURLBuilder;
import com.mercedesbenz.sechub.test.TestUtil;

public class AsPDSUser {

    private static final int DEFAULT_SECONDS_TO_WAIT_BEFORE_TIMEOUT = 10;
    TestUser user;

    AsPDSUser(TestUser user) {
        this.user = user;
    }

    private TestRestHelper getRestHelper() {
        return getContext().getPDSRestHelper(user);
    }

    private TestURLBuilder getUrlBuilder() {
        return getContext().getPDSUrlBuilder();
    }

    private IntegrationTestContext getContext() {
        return IntegrationTestContext.get();
    }

    public void markJobAsReadyToStart(UUID jobUUID) {
        TestRestHelper restHelper = getRestHelper();
        TestURLBuilder urlBuilder = getUrlBuilder();
        markJobAsReadyToStart(jobUUID, restHelper, urlBuilder);
    }

    public static void markJobAsReadyToStart(UUID jobUUID, TestRestHelper restHelper, TestURLBuilder urlBuilder) {
        restHelper.put(urlBuilder.pds().buildMarkJobReadyToStart(jobUUID));
    }

    public String getJobStatus(UUID jobUUID) {
        return getRestHelper().getJSON(getUrlBuilder().pds().buildGetJobStatus(jobUUID));
    }

    public String getJobReport(UUID jobUUID) {
        return getJobReport(jobUUID, false, DEFAULT_SECONDS_TO_WAIT_BEFORE_TIMEOUT);
    }

    public String getJobReportOrErrorText(UUID jobUUID) {
        return getJobReport(jobUUID, true, DEFAULT_SECONDS_TO_WAIT_BEFORE_TIMEOUT);
    }

    public String getJobReport(UUID jobUUID, boolean orGetErrorText, long secondsToWait) {
        long waitTimeInMillis = 1000;
        int count = 0;
        boolean jobEnded = false;
        String jobstatus = null;
        while (count < secondsToWait) {
            jobstatus = getJobStatus(jobUUID);
            if (jobstatus.indexOf("DONE") != -1) {
                jobEnded = true;
                break;
            }
            if (jobstatus.indexOf("FAILED") != -1) {
                if (!orGetErrorText) {
                    throw new IllegalStateException("Job did fail:" + jobstatus);
                }
                jobEnded = true;
                break;
            }

            TestUtil.waitMilliseconds(waitTimeInMillis);
            ++count;
        }
        if (!jobEnded) {
            throw new IllegalStateException("Even after " + count + " retries, every waiting " + waitTimeInMillis
                    + " ms, no job report state ENDED was accessible!\nLAST fetched jobstatus for " + jobUUID + " was:\n" + jobstatus);
        }
        /* okay report is available - so do download */
        if (orGetErrorText) {
            return getRestHelper().getJSON(getUrlBuilder().pds().buildGetJobResultOrErrorText(jobUUID));
        }
        return getRestHelper().getJSON(getUrlBuilder().pds().buildGetJobResult(jobUUID));
    }

    public boolean getIsAlive() {
        getRestHelper().head(getUrlBuilder().pds().buildAnonymousCheckAlive());
        return true;
    }

    public String getMonitoringStatus() {
        String url = getUrlBuilder().pds().buildAdminGetMonitoringStatus();
        String result = getRestHelper().getJSON(url);
        return result;
    }

    public AsPDSUser cancelJob(UUID jobUUID) {
        String url = getUrlBuilder().pds().buildCancelJob(jobUUID);
        getRestHelper().post(url);
        return this;
    }

    public String createJobFor(UUID sechubJobUUID, PDSIntTestProductIdentifier identifier) {
        return createJobFor(sechubJobUUID, identifier, null);
    }

    public String createJobFor(UUID sechubJobUUID, PDSIntTestProductIdentifier identifier, Map<String, String> customParameters) {

        Map<String, String> internalParameters = new LinkedHashMap<>();

        /* create default params */
        switch (identifier) {
        case PDS_INTTEST_CODESCAN:
            internalParameters.put("product1.qualititycheck.enabled", "false");
            internalParameters.put("product1.level", "1");
            break;
        case PDS_INTTEST_INFRASCAN:
        case PDS_INTTEST_WEBSCAN:
        default:
            internalParameters.put("nothing.special", "true");
        }
        if (customParameters != null) {
            internalParameters.putAll(customParameters);
        }

        return createJobFor(sechubJobUUID, identifier.getId(), internalParameters);
    }

    private String createJobFor(UUID sechubJobUUID, String productId, Map<String, String> params) {
        TestRestHelper restHelper = getRestHelper();
        TestURLBuilder urlBuilder = getUrlBuilder();
        return createJobFor(sechubJobUUID, params, productId, restHelper, urlBuilder);
    }

    public static String createJobFor(UUID sechubJobUUID, Map<String, String> params, String productId, TestRestHelper restHelper, TestURLBuilder urlBuilder) {
        String url = urlBuilder.pds().buildCreateJob();
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

        String result = restHelper.postJson(url, sb.toString());
        return result;
    }

    public AsPDSUser upload(UUID pdsJobUUID, String fileName, String pathInsideResources) {
        File uploadFile = IntegrationTestFileSupport.getTestfileSupport().createFileFromResourcePath(pathInsideResources);
        return upload(pdsJobUUID, fileName, uploadFile);
    }

    public AsPDSUser upload(UUID pdsJobUUID, String uploadName, File file) {
        upload(getUrlBuilder(), getRestHelper(), pdsJobUUID, uploadName, file);
        return this;
    }

    public String getJobOutputStreamText(UUID jobUUID) {
        String url = getUrlBuilder().pds().buildAdminFetchesJobOutputStreamUrl(jobUUID);
        String result = getRestHelper().getStringFromURL(url);
        return result;
    }

    public String getJobErrorStreamText(UUID jobUUID) {
        String url = getUrlBuilder().pds().buildAdminFetchesJobErrorStreamUrl(jobUUID);
        String result = getRestHelper().getStringFromURL(url);
        return result;
    }

    public static void upload(TestURLBuilder urlBuilder, TestRestHelper restHelper, UUID pdsJobUUID, String uploadName, File file) {
        String checkSum = TestAPI.createSHA256Of(file);
        String url = urlBuilder.pds().buildUpload(pdsJobUUID, uploadName);
        restHelper.upload(url, file, checkSum);
    }

    public String getServerConfiguration() {
        String url = getUrlBuilder().pds().buildAdminGetServerConfiguration();
        String result = getRestHelper().getJSON(url);
        return result;
    }

}
