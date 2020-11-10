// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.support;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

public class RestOperationsSupport {

    private RestOperations restOperations;

    public RestOperationsSupport(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    public String postJSON(String url, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);

        return restOperations.postForEntity(url, httpEntity, String.class).getBody();
    }

    public String upload(String uploadURL, File file, String checkSum) {
        // see https://www.baeldung.com/spring-rest-template-multipart-upload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        FileSystemResource resource = new FileSystemResource(file);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("checkSum", checkSum);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restOperations.postForEntity(uploadURL, requestEntity, String.class);
        return response.getBody();
    }

    public void put(String url) {
        restOperations.put(url, null);
    }
}
