// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.springextension;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;

/**
 * See https://github.com/spring-projects/spring-framework/issues/18147 This class
 * leaves it to the resource-implementation to decide whether it can reasonably
 * supply a content-length. It does so by assuming that returning {@code null}
 * or a negative number indicates its unwillingness to provide a content-length.
 *
 */
public class ResourceHttpMessageConverterHandlingInputStreams extends ResourceHttpMessageConverter {

    @Override
    protected Long getContentLength(Resource resource, MediaType contentType) throws IOException {
        Long contentLength = super.getContentLength(resource, contentType);

        return contentLength == null || contentLength < 0 ? null : contentLength;
    }
}