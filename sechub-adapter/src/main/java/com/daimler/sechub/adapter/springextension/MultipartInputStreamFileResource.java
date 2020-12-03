// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.springextension;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;


/**
 * See https://github.com/spring-projects/spring-framework/issues/18147
 * Works with {@link ResourceHttpMessageConverterHandlingInputStreams} to forward input stream from
 * file-uploads without reading everything into memory.
 *
 */
public class MultipartInputStreamFileResource extends InputStreamResource {

    private final String filename;

    public MultipartInputStreamFileResource(InputStream inputStream, String filename) {
        super(inputStream);
        this.filename = filename;
    }
    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
        return -1; // we do not want to generally read the whole stream into memory ...
    }
}

