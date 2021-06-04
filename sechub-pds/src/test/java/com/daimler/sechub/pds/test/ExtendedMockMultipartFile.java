// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.test;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.mock.web.MockMultipartFile;

/**
 * Based on {@link MockMultipartFile} - with some extensions:
 * <ol>
 * <li>
 *  remember last input stream fetched by {@link #getInputStream()}. This stream
 *  object can be fetched by {@link #getRememberedInputStream()}. So mockito tests
 *  can verify input stream parameters
 * </li>
 * </ol>
 * @author Albert Tregnaghi
 *
 */
public class ExtendedMockMultipartFile extends MockMultipartFile{

    private InputStream rememberedInputStream; 
    
    public ExtendedMockMultipartFile(String name, byte[] content) {
        super(name, content);
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        rememberedInputStream= super.getInputStream();
        return rememberedInputStream;
    }
    
    public InputStream getRememberedInputStream() {
        return rememberedInputStream;
    }
    
}