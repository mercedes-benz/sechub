// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import java.io.File;
import java.util.UUID;

import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;
import com.daimler.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.daimler.sechub.integrationtest.internal.TestRestHelper;
import com.daimler.sechub.test.TestURLBuilder;
import com.daimler.sechub.test.TestUtil;

public class AsPDSUser {

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
        long waitTimeInMillis = 1000;
        int count = 0;
        boolean jobEnded = false;
        String jobstatus = null;
        while (count < 10) {
            jobstatus = getJobStatus(jobUUID);
            if (jobstatus.indexOf("DONE") != -1) {
                jobEnded = true;
                break;
            }
            TestUtil.waitMilliseconds(waitTimeInMillis);
            ++count;
        }
        if (!jobEnded) {
            throw new IllegalStateException("Even after " + count + " retries, every waiting " + waitTimeInMillis
                    + " ms, no job report state ENDED was accessible!\nLAST fetched jobstatus for " + jobUUID +" was:\n"
                    + jobstatus);
        }
        /* okay report is available - so do download */
        return getRestHelper().getJSon(getUrlBuilder().pds().buildGetJobResult(jobUUID));
    }
    
    public boolean getIsAlive() {
        getRestHelper().head(getUrlBuilder().pds().buildAnonymousCheckAlive());
        return true;
    }


    public AsPDSUser cancelJob(UUID jobUUID) {
        String url = getUrlBuilder().pds().buildCancelJob(jobUUID);
        getRestHelper().post(url);
        return this;
    }
    
    public String createJobFor(UUID sechubJobUUID, PDSIntProductIdentifier identifier) {
        String url = getUrlBuilder().pds().buildCreateJob();
        String json = "{\"apiVersion\":\"1.0\",\"sechubJobUUID\":\""+sechubJobUUID.toString()+"\",\n\"productId\":\""+identifier.getId()+"\"}";
        String result = getRestHelper().postJSon(url,json);
        return result;
    }

    public AsPDSUser upload(UUID pdsJobUUID, String fileName, String pathInsideResources) {
        File uploadFile = IntegrationTestFileSupport.getTestfileSupport().createFileFromResourcePath(pathInsideResources);
        String checkSum = TestAPI.createSHA256Of(uploadFile);
        return upload(pdsJobUUID, fileName, uploadFile,checkSum);
    }

    
    public AsPDSUser upload(UUID pdsJobUUID, String uploadName, File file, String sha256CheckSum) {
        String url = getUrlBuilder().pds().buildUpload(pdsJobUUID, uploadName);
        getRestHelper().upload(url, file, sha256CheckSum);
        return this;
    }

}
