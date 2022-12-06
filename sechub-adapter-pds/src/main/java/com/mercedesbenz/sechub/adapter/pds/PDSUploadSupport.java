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
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;

public class PDSUploadSupport {

    public void upload(SecHubDataConfigurationType dataType, PDSContext context, PDSAdapterConfigData data, String checkSum, String fileSizeAsString)
            throws AdapterException {
        String uploadSourceCodeUrl = context.getUrlBuilder().buildUpload(context.getPdsJobUUID(), resolveUploadFileName(dataType));
        RestOperations restTemplate = context.getRestOperations();

        // see https://www.baeldung.com/spring-rest-template-multipart-upload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(FILE_SIZE_HEADER_FIELD_NAME, fileSizeAsString);

        Resource resource = fetchResource(dataType, context, data);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(MULTIPART_FILE, resource);
        body.add(MULTIPART_CHECKSUM, checkSum);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(uploadSourceCodeUrl, requestEntity, String.class);

        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw context.asAdapterException("Response HTTP status not 'OK' as expected but: " + response.getStatusCode());
        }
    }

    private Resource fetchResource(SecHubDataConfigurationType dataType, PDSContext context, PDSAdapterConfigData data) throws AdapterException {
        String fileName = resolveUploadFileName(dataType);
        InputStream zipInputstream = resolveInputStream(dataType, context, data);

        return new MultipartInputStreamFileResource(zipInputstream, fileName);
    }

    private String resolveUploadFileName(SecHubDataConfigurationType type) {
        String fileName = null;
        switch (type) {
        case NONE:
            return null;
        case BINARY:
            fileName = FILENAME_BINARIES_TAR;
            break;
        case SOURCE:
            fileName = FILENAME_SOURCECODE_ZIP;
            break;
        default:
            throw new IllegalStateException("unsupported data type:" + type);
        }
        return fileName;
    }

    private InputStream resolveInputStream(SecHubDataConfigurationType dataType, PDSContext context, PDSAdapterConfigData data) throws AdapterException {
        InputStream inputStream = null;
        switch (dataType) {
        case NONE:
            throw new IllegalStateException("There cannot be an inputstream for: " + dataType + ". Illegal situation - should not be called!");
        case BINARY:
            inputStream = data.getBinaryTarFileInputStreamOrNull();
            break;
        case SOURCE:
            inputStream = data.getSourceCodeZipFileInputStreamOrNull();
            break;
        default:
            throw new IllegalStateException("unsupported data type:" + dataType);
        }
        if (inputStream == null) {
            throw context.asAdapterException("Input stream for " + dataType + " file is null!");
        }
        return inputStream;
    }

}
