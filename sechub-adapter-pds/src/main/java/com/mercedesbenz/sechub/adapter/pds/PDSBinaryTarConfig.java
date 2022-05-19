// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.io.InputStream;

public interface PDSBinaryTarConfig extends PDSAdapterConfig {

    InputStream getBinaryTarFileInputStreamOrNull();

}
