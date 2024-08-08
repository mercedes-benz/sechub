// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatus;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData;
import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;
import com.mercedesbenz.sechub.integrationtest.internal.TestRestHelper;
import com.mercedesbenz.sechub.test.PDSTestURLBuilder;
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

    private PDSTestURLBuilder getPDSUrlBuilder() {
        return getContext().getPDSUrlBuilder();
    }

    private IntegrationTestContext getContext() {
        return IntegrationTestContext.get();
    }

    public void markJobAsReadyToStart(UUID jobUUID) {
        TestRestHelper restHelper = getRestHelper();
        PDSTestURLBuilder urlBuilder = getPDSUrlBuilder();
        markJobAsReadyToStart(jobUUID, restHelper, urlBuilder);
    }

    public static void markJobAsReadyToStart(UUID jobUUID, TestRestHelper restHelper, PDSTestURLBuilder urlBuilder) {
        restHelper.put(urlBuilder.buildMarkJobReadyToStart(jobUUID));
    }

    public String getJobStatus(UUID jobUUID) {
        return getRestHelper().getJSON(getPDSUrlBuilder().buildGetJobStatus(jobUUID));
    }

    public PDSJobStatusState getJobStatusState(UUID jobUUID) {
        PDSJobStatus pdsJobStatus = getJobStatusObject(jobUUID);
        return pdsJobStatus.getState();
    }

    public PDSJobStatus getJobStatusObject(UUID jobUUID) {
        String status = getJobStatus(jobUUID);
        PDSJobStatus pdsJobStatus = TestJSONHelper.get().createFromJSON(status, PDSJobStatus.class);
        return pdsJobStatus;
    }

    public String getJobReport(UUID jobUUID) {
        return getJobReport(jobUUID, false, DEFAULT_SECONDS_TO_WAIT_BEFORE_TIMEOUT);
    }

    public String getJobReportOrErrorText(UUID jobUUID) {
        return getJobReport(jobUUID, true, DEFAULT_SECONDS_TO_WAIT_BEFORE_TIMEOUT);
    }

    public String getJobReport(UUID jobUUID, boolean orGetErrorText, long secondsToWait) {
        return internalFetchReport(jobUUID, orGetErrorText, secondsToWait, true);
    }

    String internalFetchReportWithoutAutoDump(UUID jobUUID, long secondsToWait) {
        return internalFetchReport(jobUUID, false, secondsToWait, false);
    }

    private String internalFetchReport(UUID jobUUID, boolean orGetErrorText, long secondsToWait, boolean autoDumpEnabled) {
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
                    if (autoDumpEnabled) {
                        TestAPI.dumpPDSJobOutput(jobUUID);
                    }
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
            return getRestHelper().getJSON(getPDSUrlBuilder().buildGetJobResultOrErrorText(jobUUID));
        }
        return getRestHelper().getJSON(getPDSUrlBuilder().buildGetJobResult(jobUUID));
    }

    public boolean getIsAlive() {
        getRestHelper().head(getPDSUrlBuilder().buildAnonymousCheckAlive());
        return true;
    }

    public String getMonitoringStatus() {
        String url = getPDSUrlBuilder().buildAdminGetMonitoringStatus();
        String result = getRestHelper().getJSON(url);
        return result;
    }

    public AsPDSUser cancelJob(UUID jobUUID) {
        String url = getPDSUrlBuilder().buildCancelJob(jobUUID);
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

        TestRestHelper restHelper = getRestHelper();
        PDSTestURLBuilder urlBuilder = getPDSUrlBuilder();

        return TestAPI.createPDSJobFor(sechubJobUUID, internalParameters, identifier.getId(), restHelper, urlBuilder);
    }

    public String createJobByJsonConfiguration(String json) {
        TestRestHelper restHelper = getRestHelper();
        PDSTestURLBuilder urlBuilder = getPDSUrlBuilder();

        return TestAPI.createPDSJob(restHelper, urlBuilder, json);
    }

    public AsPDSUser upload(UUID pdsJobUUID, String fileName, String pathInsideResources) {
        File uploadFile = IntegrationTestFileSupport.getTestfileSupport().createFileFromResourcePath(pathInsideResources);
        return upload(pdsJobUUID, fileName, uploadFile);
    }

    public AsPDSUser upload(UUID pdsJobUUID, String uploadName, File file) {
        upload(getPDSUrlBuilder(), getRestHelper(), pdsJobUUID, uploadName, file);
        return this;
    }

    public String getJobOutputStreamText(UUID jobUUID) {
        return internalFetchOutputStreamTextWithoutAutoDump(jobUUID);
    }

    String internalFetchOutputStreamTextWithoutAutoDump(UUID jobUUID) {
        String url = getPDSUrlBuilder().buildAdminFetchesJobOutputStreamUrl(jobUUID);
        String result = getRestHelper().getStringFromURL(url);
        return result;
    }

    public String getJobErrorStreamText(UUID jobUUID) {
        return internalFetchErrorStreamTextWithoutAutoDump(jobUUID);
    }

    String internalFetchErrorStreamTextWithoutAutoDump(UUID jobUUID) {
        String url = getPDSUrlBuilder().buildAdminFetchesJobErrorStreamUrl(jobUUID);
        String result = getRestHelper().getStringFromURL(url);
        return result;
    }

    public static void upload(PDSTestURLBuilder urlBuilder, TestRestHelper restHelper, UUID pdsJobUUID, String uploadName, File file) {
        String checkSum = TestAPI.createSHA256Of(file);
        upload(urlBuilder, restHelper, pdsJobUUID, uploadName, file, checkSum);
    }

    public AsPDSUser uploadWithWrongChecksum(UUID pdsJobUUID, String uploadName, String pathInsideResources) {
        File uploadFile = IntegrationTestFileSupport.getTestfileSupport().createFileFromResourcePath(pathInsideResources);
        return uploadWithWrongChecksum(pdsJobUUID, uploadName, uploadFile);
    }

    public AsPDSUser uploadWithWrongChecksum(UUID pdsJobUUID, String uploadName, File file) {
        upload(getPDSUrlBuilder(), getRestHelper(), pdsJobUUID, uploadName, file, "wrong-checksum");
        return this;
    }

    private static void upload(PDSTestURLBuilder urlBuilder, TestRestHelper restHelper, UUID pdsJobUUID, String uploadName, File file, String checkSum) {
        String url = urlBuilder.buildUpload(pdsJobUUID, uploadName);
        restHelper.upload(url, file, checkSum);
    }

    public String getServerConfiguration() {
        String url = getPDSUrlBuilder().buildAdminGetServerConfiguration();
        String result = getRestHelper().getJSON(url);
        return result;
    }

    public void updateAutoCleanupConfiguration(TestAutoCleanupData data) {
        String json = TestJSONHelper.get().createJSON(data);
        updateAutoCleanupConfiguration(json);

    }

    public void updateAutoCleanupConfiguration(String json) {
        String url = getPDSUrlBuilder().buildAdminUpdatesAutoCleanupConfigurationUrl();
        getRestHelper().putJSON(url, json);
    }

    public TestAutoCleanupData fetchAutoCleanupConfiguration() {
        String url = getPDSUrlBuilder().buildAdminFetchesAutoCleanupConfigurationUrl();

        String json = getRestHelper().getJSON(url);
        return TestJSONHelper.get().createFromJSON(json, TestAutoCleanupData.class);
    }

    public SecHubMessagesList getJobMessages(UUID pdsJobUUID) {
        return internalGetJobMessagesWithoutAutoDump(pdsJobUUID);
    }

    SecHubMessagesList internalGetJobMessagesWithoutAutoDump(UUID pdsJobUUID) {
        String url = getPDSUrlBuilder().buildGetJobMessages(pdsJobUUID);
        String json = getRestHelper().getJSON(url);
        return SecHubMessagesList.fromJSONString(json);
    }

    public String getJobMetaData(UUID pdsJobUUID) {
        String url = getPDSUrlBuilder().buildAdminFetchesJobMetaData(pdsJobUUID);
        String text = getRestHelper().getStringFromURL(url);
        return text;
    }

    PDSJobStatus internalFetchStatusWithoutAutoDump(UUID jobUUID) {
        String url = getPDSUrlBuilder().buildGetJobStatus(jobUUID);
        String json = getRestHelper().getJSON(url);
        return TestJSONHelper.get().createFromJSON(json, PDSJobStatus.class);
    }

}
