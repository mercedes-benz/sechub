// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.test.JSONTestUtil;
import com.mercedesbenz.sechub.test.TestVerifier;
import com.mercedesbenz.sechub.test.WiremockUrlHistory;

public class TestPDSWiremockSupport {

    private boolean pdsJobCanBeCreated;
    private boolean pdsMarkReadyToStart;
    private GetResult pdsFetchJobResult;
    private WiremockUrlHistory history;
    private static final String APPLICATION_JSON = "application/json";

    private WireMockServer server;
    private PDSUrlBuilder pdsURLBuilder;
    public UUID sechubJobUUID;
    public String pdsProductIdentifier;
    public Map<String, String> pdsJobParameters;
    public SecHubMessagesList sechubMessageList;
    private TestPDSSupport testPDSSupport;
    private UUID pdsJobUUID;
    private List<UploadInfo> uploads = new ArrayList<>();
    private List<StateQueueInfo> stateRequestsResults = new ArrayList<>();

    private TestVerifier testVerifier = new TestVerifier();
    public boolean useSecHubStorage;

    public TestPDSWiremockSupport(WireMockServer server) {
        this.server = server;
        this.history = new WiremockUrlHistory();
        this.pdsURLBuilder = new PDSUrlBuilder("");
        this.testPDSSupport = new TestPDSSupport();
    }

    private static class UploadInfo {
        String fileName;
        long uploadSizeInBytes;

        public long getUploadSizeInBytes() {
            return uploadSizeInBytes;
        }
    }

    private static class StateQueueInfo {
        PDSJobStatusState state;
    }

    private static class GetResult {
        String result;
        int exitCode;
        boolean failed;
    }

    public static PDSWiremockTestSupportBuilder builder(WireMockServer server) {
        return new PDSWiremockTestSupportBuilder(server);
    }

    public void startPDSServerSimulation() {

        try {
            if (pdsJobCanBeCreated) {
                simulationJobCreation();
            }
            for (UploadInfo info : uploads) {
                simulateUploadData(info);
            }
            if (pdsMarkReadyToStart) {
                simulateMarkReadyToStart();
            }
            for (StateQueueInfo info : stateRequestsResults) {
                simulateStateRequest(info);
            }
            if (pdsFetchJobResult != null) {
                simulateGetResult(pdsFetchJobResult);
                simulateGetMessages(sechubMessageList);
            }

        } catch (Exception e) {
            throw new RuntimeException("unexpected:" + e.getMessage(), e);
        }

    }

    private void simulateGetMessages(SecHubMessagesList sechubMessageList) {
        if (pdsJobUUID == null) {
            throw new IllegalStateException("testcase corrupt? pds job uuid not known here!");
        }
        if (pdsFetchJobResult == null) {
            throw new IllegalStateException("pdsFetchJobResult may not be null here!");
        }
        /* @formatter:off */
        String url = pdsURLBuilder.buildGetJobMessages(pdsJobUUID);
        if (sechubMessageList==null) {
            sechubMessageList=new SecHubMessagesList();
        }
        stubFor(get(urlEqualTo(history.rememberGET(url))).
                willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody(sechubMessageList.toJSON()))
                );
        /* @formatter:on */
        testVerifier.add(() -> verify(getRequestedFor(urlEqualTo(url))));
    }

    private void simulateGetResult(GetResult pdsFetchJobResult) {
        if (pdsJobUUID == null) {
            throw new IllegalStateException("testcase corrupt? pds job uuid not known here!");
        }
        if (pdsFetchJobResult == null) {
            throw new IllegalStateException("pdsFetchJobResult may not be null here!");
        }
        /* @formatter:off */
        String url = pdsURLBuilder.buildGetJobResult(pdsJobUUID);
        String jobResultFailedString = Boolean.valueOf(pdsFetchJobResult.failed).toString().toLowerCase();

        stubFor(get(urlEqualTo(history.rememberGET(url))).
                willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody("{\"exitCode\" : "+pdsFetchJobResult.exitCode+", \"failed\" : \""+jobResultFailedString+"\", \"result\" : \""+pdsFetchJobResult.result+"\" }"))
                );
        /* @formatter:on */

        testVerifier.add(() -> verify(getRequestedFor(urlEqualTo(url))));
    }

    private void simulateStateRequest(StateQueueInfo info) {
        if (pdsJobUUID == null) {
            throw new IllegalStateException("testcase corrupt? pds job uuid not known here!");
        }
        /* @formatter:off */
        String url = pdsURLBuilder.buildGetJobStatus(pdsJobUUID);
        stubFor(get(urlEqualTo(history.rememberGET(url))).
                willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody("{\"jobUUID\" : \"" + pdsJobUUID.toString() + "\", \"state\":\""+info.state+"\" }"))
                );
        /* @formatter:on */
    }

    private void simulateMarkReadyToStart() {
        if (pdsJobUUID == null) {
            throw new IllegalStateException("testcase corrupt? pds job uuid not known here!");
        }
        /* @formatter:off */
        String url = pdsURLBuilder.buildMarkJobReadyToStart(pdsJobUUID);
        stubFor(put(urlEqualTo(history.rememberPUT(url))).withHeader("content-type", equalTo(APPLICATION_JSON))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", APPLICATION_JSON)));
        /* @formatter:on */

        testVerifier.add(() -> verify(putRequestedFor(urlEqualTo(url))));
    }

    private void simulateUploadData(UploadInfo info) {
        if (pdsJobUUID == null) {
            throw new IllegalStateException("testcase corrupt? pds job uuid not known here!");
        }
        String uploadFileName = info.fileName;

        /* @formatter:off */
        String url = pdsURLBuilder.buildUpload(pdsJobUUID, uploadFileName);
        stubFor(post(urlEqualTo(history.rememberPOST(url))).
                withHeader("content-type",
                        containing("multipart/form-data;boundary=")).
                withHeader("x-file-size",
                        containing(""+info.getUploadSizeInBytes())).
                withMultipartRequestBody(
                        aMultipart().withName("checkSum")).
                withMultipartRequestBody(
                        aMultipart().withName("file")).

                willReturn(aResponse().
                            withStatus(HttpStatus.OK.value()).
                            withHeader("Content-Type", APPLICATION_JSON).
                            withBody("{\"jobUUID\" : \"" + pdsJobUUID.toString() + "\"}")));
        /* @formatter:on */

        testVerifier.add(() -> verify(postRequestedFor(urlEqualTo(url))));
    }

    private void simulationJobCreation() throws Exception {

        pdsJobUUID = UUID.randomUUID();

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        String url = pdsURLBuilder.buildCreateJob();
        map.put("apiVersion", "1.0");
        map.put("sechubJobUUID", sechubJobUUID.toString());
        map.put("productId", pdsProductIdentifier);

        map.put("parameters", testPDSSupport.toKeyValue(pdsJobParameters));

        /* @formatter:off */
        String requestBody = JSONTestUtil.toJSONContainingNullValues(map);
        stubFor(post(urlEqualTo(history.rememberPOST(url)))
                .withHeader("content-type", equalTo(APPLICATION_JSON)).
                withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse().
                         withStatus(HttpStatus.OK.value()).
                         withHeader("Content-Type", APPLICATION_JSON)
                        .withBody("{\"jobUUID\" : \""+pdsJobUUID.toString()+"\"}")));
        /* @formatter:on */

        testVerifier.add(() -> verify(postRequestedFor(urlEqualTo(url))));

    }

    public static class PDSWiremockTestSupportBuilder {

        private TestPDSWiremockSupport current;

        public PDSWiremockTestSupportBuilder(WireMockServer server) {
            current = new TestPDSWiremockSupport(server);
        }

        public PDSWiremockTestSupportBuilder simulateJobCanBeCreated(UUID sechubJobUUID, String pdsProductIdentifier, Map<String, String> parameters) {
            current.pdsJobCanBeCreated = true;
            current.sechubJobUUID = sechubJobUUID;
            current.pdsProductIdentifier = pdsProductIdentifier;
            current.pdsJobParameters = parameters;

            if (parameters != null) {
                current.useSecHubStorage = Boolean.parseBoolean(parameters.get(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE));
            }
            return this;
        }

        public PDSWiremockTestSupportBuilder simulateUploadData(String fileName, long size) {
            UploadInfo info = new UploadInfo();
            info.fileName = fileName;
            info.uploadSizeInBytes = size;
            current.uploads.add(info);
            return this;
        }

        public PDSWiremockTestSupportBuilder simulateMarkReadyToStart() {
            current.pdsMarkReadyToStart = true;
            return this;
        }

        public PDSWiremockTestSupportBuilder simulateFetchJobStatus(PDSJobStatusState status) {
            StateQueueInfo info = new StateQueueInfo();
            info.state = status;
            current.stateRequestsResults.add(info);
            return this;
        }

        public PDSWiremockTestSupportBuilder simulateFetchJobResultOk(String result) {
            return simulateFetchJobResult(result, false, 0);
        }

        public PDSWiremockTestSupportBuilder simulateFetchJobResult(String result, boolean failed, int exitCode) {
            GetResult getResult = new GetResult();
            getResult.exitCode = exitCode;
            getResult.failed = failed;
            getResult.result = result;

            current.pdsFetchJobResult = getResult;
            return this;
        }

        public PDSWiremockTestSupportBuilder simulateFetchJobMessages() {
            return simulateFetchJobMessages(null);
        }

        public PDSWiremockTestSupportBuilder simulateFetchJobMessages(SecHubMessagesList messageList) {
            current.sechubMessageList = messageList;
            return this;
        }

        TestPDSWiremockSupport build() {

            TestPDSWiremockSupport buildResult = current;
            current = new TestPDSWiremockSupport(buildResult.server);
            return buildResult;
        }

    }

    public String getTestBaseUrl() {
        return server.baseUrl();
    }

    /**
     * Verify the stubbing is called as expected
     */
    public void verfifyExpectedCalls() {
        testVerifier.verify();
    }

}
