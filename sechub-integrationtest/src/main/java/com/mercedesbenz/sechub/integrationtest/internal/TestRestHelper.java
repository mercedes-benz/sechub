// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILE_SIZE_HEADER_FIELD_NAME;
import static com.mercedesbenz.sechub.commons.core.CommonConstants.MULTIPART_CHECKSUM;
import static com.mercedesbenz.sechub.commons.core.CommonConstants.MULTIPART_FILE;
import static org.springframework.http.HttpEntity.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.adapter.TrustAllConfig;
import com.mercedesbenz.sechub.adapter.support.TrustAllSupport;
import com.mercedesbenz.sechub.integrationtest.api.UserContext;

public class TestRestHelper {

    public static enum RestHelperTarget {
        SECHUB_SERVER, SECHUB_PDS,
    }

    private static final Logger LOG = LoggerFactory.getLogger(TestRestHelper.class);

    protected RestTemplate template;
    private TrustAllSupport trustAllSupport;

    private static String LAST_DATA;
    private static String LAST_URL;

    public static String getLastUrl() {
        return LAST_URL;
    }

    public static String getLastData() {
        return LAST_DATA;
    }

    public TestRestHelper(UserContext user, RestHelperTarget target) {
        this.template = createTrustAllRestTemplate(user);
        this.template.setErrorHandler(createErrorHandler());
    }

    protected ResponseErrorHandler createErrorHandler() {
        return new ErrorHandler();
    }

    public RestTemplate getTemplate() {
        return template;
    }

    /**
     * This is a shortcut for {@link #getStringFromURL(String),
     * MediaType.APPLICATION_JSON)}.
     *
     * @param url the url to fetch JSON by HTTP GET request
     * @return result as JSON
     */
    public String getJSON(String url) {
        return getStringFromURL(url, MediaType.APPLICATION_JSON);
    }

    /**
     * Returns a string representation from URL for given accepted media types.
     *
     * @param url      the url to fetch string by HTTP GET request
     * @param accepted the list of accepted media types. If empty the header will
     *                 not contain an accepted media type, means accepting
     *                 everything
     * @return result as string
     */
    public String getStringFromURL(String url, MediaType... accepted) {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> types = new ArrayList<>();
        for (MediaType type : accepted) {
            types.add(type);
        }
        headers.setAccept(types);
        return getStringWithHeaders(url, headers);
    }

    public void put(String url) {
        put(url, null);
    }

    public void put(String url, Object request) {
        markLastURL(url);
        template.put(url, request);
    }

    /**
     * Post json to url
     *
     * @param url
     * @param json
     * @return result
     * @throws RestClientException
     */
    public String postJson(String url, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);

        markLastURL(url, json);
        return template.postForEntity(url, httpEntity, String.class).getBody();
    }

    /**
     * Post string to url
     *
     * @param url
     * @param json
     * @return result
     * @throws RestClientException
     */
    public String postPlainText(String url, String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> httpEntity = new HttpEntity<>(text, headers);

        markLastURL(url, text);
        return template.postForEntity(url, httpEntity, String.class).getBody();
    }

    /**
     * PUT json to url
     *
     * @param url
     * @param json
     * @return result
     * @throws RestClientException
     */
    public String patchJSON(String url, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);

        markLastURL(url, json);
        return template.patchForObject(url, httpEntity, String.class);
    }

    /**
     * PUT plain text to url
     *
     * @param url
     * @param json
     * @return result
     * @throws RestClientException
     */
    public String putPlainText(String url, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);

        markLastURL(url, json);
        template.put(url, httpEntity);
        return "";
    }

    /**
     * PUT json to url
     *
     * @param url
     * @param json
     * @return result
     * @throws RestClientException
     */
    public String putJSON(String url, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);

        markLastURL(url, json);
        template.put(url, httpEntity);
        return "";
    }

    private TrustAllSupport getTrustAllSupport() {
        if (trustAllSupport == null) {

            IntegrationTestAdapter adapter = new IntegrationTestAdapter();
            TrustAllConfig config = new IntegrationTestTrustAllConfig();

            trustAllSupport = new TrustAllSupport(adapter, config);
        }
        return trustAllSupport;
    }

    /**
     * Method creates a rest template.<br>
     * We cannot use the normal rest template builder of spring because of setting
     * the request factory directly is not possible by builder
     */
    protected RestTemplate createTrustAllRestTemplate(UserContext user) {

        ClientHttpRequestFactory requestTrustAllFactory = getTrustAllSupport().createTrustAllFactory();

        RestTemplate template = new RestTemplate(new BufferingClientHttpRequestFactory(requestTrustAllFactory));
        // template.setMessageConverters(createRestTemplateMessageConverters());
        template.setInterceptors(createRestTemplateInterceptors(user));

        return template;
    }

    private List<ClientHttpRequestInterceptor> createRestTemplateInterceptors(UserContext user) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new TestSecHubRestAPIClientHttpRequestInterceptor(user));
        return interceptors;
    }

    public void post(String url) {
        markLastURL(url);
        getTemplate().postForLocation(url, EMPTY);
    }

    public HttpHeaders head(String url) {
        markLastURL(url);
        return getTemplate().headForHeaders(url);
    }

    private void markLastURL(String url) {
        markLastURL(url, null);
    }

    private void markLastURL(String url, String data) {
        LAST_URL = url;
        LAST_DATA = data;
    }

    public void delete(String url) {
        markLastURL(url);
        getTemplate().delete(url);

    }

    public String getStringFromURL(String link) {
        markLastURL(link);
        return template.getForEntity(link, String.class).getBody();
    }

    public void sendGetRequestToURI(URI uri) {
        markLastURL(uri.toString());
        template.getForEntity(uri, String.class);
    }

    public String headStringFromURL(String url) {
        markLastURL(url);
        return template.headForHeaders(url).toSingleValueMap().toString();
    }

    public long getLongFromURL(String url) {
        String dataAsString = getStringFromURL(url);
        return Long.parseLong(dataAsString);
    }

    public boolean getBooleanFromURL(String url) {
        String dataAsString = getStringFromURL(url);
        return Boolean.parseBoolean(dataAsString);
    }

    public UUID getUUIDFromURL(String url) {
        String dataAsString = getStringFromURL(url);
        if (dataAsString == null) {
            return null;
        }
        return UUID.fromString(dataAsString);
    }

    public String upload(String uploadUrl, File file, String checkSum) {
        // see https://www.baeldung.com/spring-rest-template-multipart-upload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        String fileSize = String.valueOf(file.length());
        headers.set(FILE_SIZE_HEADER_FIELD_NAME, fileSize);

        FileSystemResource resource = new FileSystemResource(file);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(MULTIPART_FILE, resource);
        body.add(MULTIPART_CHECKSUM, checkSum);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        markLastURL(uploadUrl);
        ResponseEntity<String> response = template.postForEntity(uploadUrl, requestEntity, String.class);
        return response.getBody();
    }

    private String getStringWithHeaders(String url, HttpHeaders headers) {
        markLastURL(url);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = template.exchange(url, HttpMethod.GET, entity, String.class);
        return result.getBody();
    }

    private class ErrorHandler extends DefaultResponseErrorHandler {

        @Override
        protected void handleError(ClientHttpResponse response, HttpStatusCode statusCode) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getBody()))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
            } catch (Exception e) {
                sb.append("Failed:" + e.getMessage());
            }
            LOG.error(">> HTTP ERROR");
            LOG.error("     STATUS : {}", statusCode);
            LOG.error("     CONTENT: {}", sb.toString());
            try {
                super.handleError(response, statusCode);
            } catch (IOException e) {
                throw new IOException("REST call to integration test server failed for\nURL:" + getLastUrl() + "\nDATA:" + getLastData() + "\nSTATUSCODE:"
                        + statusCode + "\nRESPONSE:" + sb.toString(), e);
            }
        }
    }

}