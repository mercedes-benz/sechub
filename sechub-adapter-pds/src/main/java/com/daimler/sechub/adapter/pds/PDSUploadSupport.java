// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.io.InputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.AdapterException;

public class PDSUploadSupport {

    public void uploadZippedSourceCode(PDSContext context, PDSSourceZipConfig zipConfig) throws AdapterException {
        String checksum = zipConfig.getSourceCodeZipFileChecksum();
        
        upload(context, zipConfig, checksum);
    }

    private void upload(PDSContext context, PDSSourceZipConfig zipConfig, String checksum) throws AdapterException {
        // see https://www.baeldung.com/spring-rest-template-multipart-upload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        Resource resource = fetchResource(context, zipConfig);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("checkSum", checksum);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String url = context.getUrlBuilder().buildUpload(context.getPdsJobUUID(), "sourcecode.zip");

        RestOperations restTemplate = context.getRestOperations();
        // currently PDS server always gives response on upload time:
        // org.springframework.web.multipart.MultipartException: Current request is not a multipart request
//        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw context.asAdapterException("Response HTTP status not 'OK' as expected but: " + response.getStatusCode());
        }
    }

    private Resource fetchResource(PDSContext context, PDSSourceZipConfig config) throws AdapterException {
        InputStream zipInputstream = config.getSourceCodeZipFileInputStream();
        if (zipInputstream == null) {
            throw context.asAdapterException("Input stream containing zip file is null!");
        }
        return new InputStreamResource(zipInputstream);
    }
    
}
