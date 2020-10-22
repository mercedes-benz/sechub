package com.daimler.sechub.adapter.pds;

import java.io.InputStream;

public interface PDSCodeScanConfig extends PDSAdapterConfig{

    InputStream getSourceCodeZipFileInputStream();
}
