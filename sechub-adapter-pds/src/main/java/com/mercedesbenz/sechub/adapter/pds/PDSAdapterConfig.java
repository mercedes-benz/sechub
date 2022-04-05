// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.adapter.AdapterConfig;

public interface PDSAdapterConfig extends AdapterConfig {

    Map<String, String> getJobParameters();

    UUID getSecHubJobUUID();

    String getPdsProductIdentifier();

}