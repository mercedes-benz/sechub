// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.io.InputStream;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.springextension.MultipartInputStreamFileResource;

public class PDSUploadSupport {

    private static final String SOURCECODE_ZIP = "sourcecode.zip";

    public void uploadZippedSourceCode(PDSContext context, PDSSourceZipConfig zipConfig) throws AdapterException {
        String checksum = zipConfig.getSourceCodeZipFileChecksum();
        
        upload(context, zipConfig, checksum);
    }

    private void upload(PDSContext context, PDSSourceZipConfig zipConfig, String checkSum) throws AdapterException {
        String uploadSourceCodeUrl = context.getUrlBuilder().buildUpload(context.getPdsJobUUID(), SOURCECODE_ZIP);
        RestOperations restTemplate = context.getRestOperations();

        // see https://www.baeldung.com/spring-rest-template-multipart-upload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        Resource resource = fetchResource(context, zipConfig);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("checkSum", checkSum);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(uploadSourceCodeUrl, requestEntity, String.class);
        
        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw context.asAdapterException("Response HTTP status not 'OK' as expected but: " + response.getStatusCode());
        }
    }

    private Resource fetchResource(PDSContext context, PDSSourceZipConfig config) throws AdapterException {
        InputStream zipInputstream = config.getSourceCodeZipFileInputStream();
        if (zipInputstream == null) {
            throw context.asAdapterException("Input stream containing zip file is null!");
        }
        return new MultipartInputStreamFileResource(zipInputstream, SOURCECODE_ZIP);
    }
    
    
}
