// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.io.InputStream;

public interface PDSSourceZipConfig extends PDSAdapterConfig {

    InputStream getSourceCodeZipFileInputStreamOrNull();

    String getSourceCodeZipFileChecksumOrNull();
}
