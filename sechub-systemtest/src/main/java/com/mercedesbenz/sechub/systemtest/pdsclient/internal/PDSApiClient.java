// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.pdsclient.internal;

import java.net.http.HttpClient.Builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiClient;

/**
 * Here we just reuse the generated api client from java library to save code...
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSApiClient extends ApiClient {

    public PDSApiClient(Builder builder, ObjectMapper mapper, String baseUri) {
        super(builder, mapper, baseUri);
    }

}
