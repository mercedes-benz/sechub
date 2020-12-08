// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.io.InputStream;

public interface PDSSourceZipConfig extends PDSAdapterConfig{

    InputStream getSourceCodeZipFileInputStream();
    
    String getSourceCodeZipFileChecksum();
}
