// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

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

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.springextension.MultipartInputStreamFileResource;
import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;

public class PDSUploadSupport {

    public void upload(SecHubDataConfigurationType dataType, PDSContext context, PDSAdapterConfigData data, String checkSum) throws AdapterException {
        String uploadSourceCodeUrl = context.getUrlBuilder().buildUpload(context.getPdsJobUUID(), FILENAME_SOURCECODE_ZIP);
        RestOperations restTemplate = context.getRestOperations();

        // see https://www.baeldung.com/spring-rest-template-multipart-upload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        Resource resource = fetchResource(dataType, context, data);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(CommonConstants.MULTIPART_FILE, resource);
        body.add(CommonConstants.MULTIPART_CHECKSUM, checkSum);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(uploadSourceCodeUrl, requestEntity, String.class);

        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw context.asAdapterException("Response HTTP status not 'OK' as expected but: " + response.getStatusCode());
        }
    }

    private Resource fetchResource(SecHubDataConfigurationType dataType, PDSContext context, PDSAdapterConfigData data) throws AdapterException {
        InputStream zipInputstream = null;
        String fileName = null;
        switch (dataType) {
        case BINARY:
            zipInputstream = data.getBinaryTarFileInputStreamOrNull();
            fileName = FILENAME_SOURCECODE_ZIP;
            break;
        case SOURCE:
            zipInputstream = data.getSourceCodeZipFileInputStreamOrNull();
            fileName = FILENAME_BINARIES_TAR;
            break;
        default:
            throw new IllegalStateException("unsupported data type:" + dataType);
        }
        if (zipInputstream == null) {
            throw context.asAdapterException("Input stream for " + dataType + " file is null!");
        }
        return new MultipartInputStreamFileResource(zipInputstream, fileName);
    }

}
