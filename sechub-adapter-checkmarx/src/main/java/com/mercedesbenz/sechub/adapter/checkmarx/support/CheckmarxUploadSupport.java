// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

import java.io.InputStream;

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

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterContext;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxContext;

public class CheckmarxUploadSupport {

    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/223313947/Upload+Source+Code+Zip+File+-+POST+projects+id+sourceCode+attachments
    // POST /projects/{id}/sourceCode/attachments and upload the zipped source code
    // https://www.baeldung.com/spring-rest-template-multipart-upload
    public void uploadZippedSourceCode(CheckmarxContext context) throws AdapterException {
        CheckmarxAdapterConfig config = context.getConfig();

        Resource sourceCodeFile = fetchResource(context, config);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("zippedSource", sourceCodeFile);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String url = context.getAPIURL("projects/" + context.getSessionData().getProjectId() + "/sourceCode/attachments");

        RestOperations restTemplate = context.getRestOperations();

        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        HttpStatus expectedStatus = HttpStatus.NO_CONTENT;
        if (!result.getStatusCode().equals(expectedStatus)) {
            throw context.asAdapterException(
                    CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX + "HTTP status=" + result.getStatusCode() + " (expected was HTTP status=" + expectedStatus + ")");
        }
    }

    private Resource fetchResource(CheckmarxAdapterContext context, CheckmarxAdapterConfig config) throws AdapterException {
        InputStream zipInputstream = config.getSourceCodeZipFileInputStream();
        if (zipInputstream == null) {
            throw context.asAdapterException("Input stream containing zip file is null!");
        }
        return new InputStreamResource(zipInputstream);
    }
}
