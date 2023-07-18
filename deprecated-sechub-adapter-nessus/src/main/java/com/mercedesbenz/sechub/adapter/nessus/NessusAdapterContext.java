// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.nessus;

import com.mercedesbenz.sechub.adapter.AdapterContext;

public interface NessusAdapterContext extends AdapterContext<NessusAdapterConfig> {

    String getNessusPolicyUID();

    void setNessusPolicyId(String nessusPolicyUID);

    void setNessusSessionToken(String token);

    String getNessusSessionToken();

    void setNessusScanId(Long scanId);

    Long getNessusScanId();

    String getHistoryId();

    void setHistoryId(String id);

    String getExportFileId();

}