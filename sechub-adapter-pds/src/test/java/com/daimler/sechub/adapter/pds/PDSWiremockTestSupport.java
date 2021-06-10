// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.daimler.sechub.adapter.pds.data.PDSJobStatus.PDSAdapterJobStatusState;
import com.daimler.sechub.test.JSONTestUtil;
import com.daimler.sechub.test.WiremockUrlHistory;
import com.github.tomakehurst.wiremock.WireMockServer;

public class PDSWiremockTestSupport {

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
    private PDSTestSupport pdsTestSupport;
    private UUID pdsJobUUID;
    private List<UploadInfo> uploads = new ArrayList<>();
    private List<StateQueueInfo> stateRequestsResults = new ArrayList<>();

    public PDSWiremockTestSupport(WireMockServer server) {
        this.server = server;
        this.history = new WiremockUrlHistory();
        this.pdsURLBuilder = new PDSUrlBuilder("");
        this.pdsTestSupport = new PDSTestSupport();
    }

    private static class UploadInfo {
        String fileName;
    }

    private static class StateQueueInfo {
        PDSAdapterJobStatusState state;
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
                simulateUploadData(info.fileName);
            }
            if (pdsMarkReadyToStart) {
                simulateMarkReadyToStart();
            }
            for (StateQueueInfo info : stateRequestsResults) {
                simulateStateRequest(info);
            }
            if (pdsFetchJobResult != null) {
                simulateGetResult(pdsFetchJobResult);
            }

        } catch (Exception e) {
            throw new RuntimeException("unexpected:" + e.getMessage(), e);
        }

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
    }

    private void simulateUploadData(String uploadFileName) {

        if (pdsJobUUID == null) {
            throw new IllegalStateException("testcase corrupt? pds job uuid not known here!");
        }
        /* @formatter:off */
        String url = pdsURLBuilder.buildUpload(pdsJobUUID, uploadFileName);
        stubFor(post(urlEqualTo(history.rememberPOST(url)))
                .withHeader("content-type", containing("multipart/form-data;charset=UTF-8")).withRequestBody(containing(uploadFileName))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", APPLICATION_JSON)
                        .withBody("{\"jobUUID\" : \"" + pdsJobUUID.toString() + "\"}")));
        /* @formatter:on */
    }

    private void simulationJobCreation() throws Exception {

        pdsJobUUID = UUID.randomUUID();

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        String url = pdsURLBuilder.buildCreateJob();
        map.put("apiVersion", "1.0");
        map.put("sechubJobUUID", sechubJobUUID.toString());
        map.put("productId", pdsProductIdentifier);

        map.put("parameters", pdsTestSupport.toKeyValue(pdsJobParameters));

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

    }

    public static class PDSWiremockTestSupportBuilder {

        private PDSWiremockTestSupport current;

        public PDSWiremockTestSupportBuilder(WireMockServer server) {
            current = new PDSWiremockTestSupport(server);
        }

        public PDSWiremockTestSupportBuilder simulateJobCanBeCreated(UUID sechubJobUUID, String pdsProductIdentifier, Map<String, String> parameters) {
            current.pdsJobCanBeCreated = true;
            current.sechubJobUUID = sechubJobUUID;
            current.pdsProductIdentifier = pdsProductIdentifier;
            current.pdsJobParameters = parameters;
            return this;
        }

        public PDSWiremockTestSupportBuilder simulateUploadData(String fileName) {
            UploadInfo info = new UploadInfo();
            info.fileName = fileName;
            current.uploads.add(info);
            return this;
        }

        public PDSWiremockTestSupportBuilder simulateMarkReadyToStart() {
            current.pdsMarkReadyToStart = true;
            return this;
        }

        public PDSWiremockTestSupportBuilder simulateFetchJobStatus(PDSAdapterJobStatusState status) {
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

        PDSWiremockTestSupport build() {

            PDSWiremockTestSupport buildResult = current;
            current = new PDSWiremockTestSupport(buildResult.server);
            return buildResult;
        }
    }

    public String getTestBaseUrl() {
        return server.baseUrl();
    }

}
