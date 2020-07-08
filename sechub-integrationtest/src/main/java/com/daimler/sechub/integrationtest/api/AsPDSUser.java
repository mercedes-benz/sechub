// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.daimler.sechub.integrationtest.JSONTestSupport;
import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;
import com.daimler.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.daimler.sechub.integrationtest.internal.TestRestHelper;
import com.daimler.sechub.test.TestURLBuilder;
import com.daimler.sechub.test.TestUtil;

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
        getRestHelper().put(getUrlBuilder().pds().buildMarkJobReadyToStart(jobUUID));
    }

    public String getJobStatus(UUID jobUUID) {
        return getRestHelper().getJSon(getUrlBuilder().pds().buildGetJobStatus(jobUUID));
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
            return getRestHelper().getJSon(getUrlBuilder().pds().buildGetJobResultOrErrorText(jobUUID));
        }
        return getRestHelper().getJSon(getUrlBuilder().pds().buildGetJobResult(jobUUID));
    }

    public boolean getIsAlive() {
        getRestHelper().head(getUrlBuilder().pds().buildAnonymousCheckAlive());
        return true;
    }

    public String getExecutionStatus() {
        String url = getUrlBuilder().pds().buildAdminGetExecutionStatus();
        String result = getRestHelper().getJSon(url);
        return result;
    }

    public AsPDSUser cancelJob(UUID jobUUID) {
        String url = getUrlBuilder().pds().buildCancelJob(jobUUID);
        getRestHelper().post(url);
        return this;
    }

    public String createJobFor(UUID sechubJobUUID, PDSIntProductIdentifier identifier) {
        Map<String, String> params = new LinkedHashMap<>();

        /* create default params */
        switch (identifier) {
        case PDS_INTTEST_CODESCAN:
            params.put("product1.qualititycheck.enabled", "false");
            params.put("product1.level", "1");
            break;
        case PDS_INTTEST_INFRASCAN:
        case PDS_INTTEST_WEBSCAN:
        default:
            params.put("nothing.special", "true");
        }

        return createJobFor(sechubJobUUID, identifier, params);
    }

    /*
     * '* "mandatory": [ { "key": "product1.qualititycheck.enabled", "description":
     * "when 'true' quality scan results are added as well" }, { "key":
     * "product1.level", "description":
     * "numeric, 1-gets all, 2-only critical,fatal and medium, 3- only critical and fatal"
     * } ], "optional": [ { "key": "product1.add.tipoftheday", "description":
     * "add tip of the day as info" } ]
     */
    public String createJobFor(UUID sechubJobUUID, PDSIntProductIdentifier identifier, Map<String, String> params) {
        String url = getUrlBuilder().pds().buildCreateJob();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"apiVersion\":\"1.0\",\"sechubJobUUID\":\"").append(sechubJobUUID.toString()).append("\",\"productId\":\"").append(identifier.getId())
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

        String result = getRestHelper().postJSon(url, sb.toString());
        return result;
    }

    public AsPDSUser upload(UUID pdsJobUUID, String fileName, String pathInsideResources) {
        File uploadFile = IntegrationTestFileSupport.getTestfileSupport().createFileFromResourcePath(pathInsideResources);
        String checkSum = TestAPI.createSHA256Of(uploadFile);
        return upload(pdsJobUUID, fileName, uploadFile, checkSum);
    }

    public AsPDSUser upload(UUID pdsJobUUID, String uploadName, File file, String sha256CheckSum) {
        String url = getUrlBuilder().pds().buildUpload(pdsJobUUID, uploadName);
        getRestHelper().upload(url, file, sha256CheckSum);
        return this;
    }

    public String getServerConfiguration() {
        String url = getUrlBuilder().pds().buildAdminGetServerConfiguration();
        String result = getRestHelper().getJSon(url);
        return result;
    }

}
