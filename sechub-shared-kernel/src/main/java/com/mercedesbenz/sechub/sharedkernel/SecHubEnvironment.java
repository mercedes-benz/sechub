// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import static com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

@Component
public class SecHubEnvironment {

    @Value("${sechub.server.baseurl}")
    @MustBeDocumented(value = "Base url of SecHub server - e.g. https://sechub.example.org", scope = SCOPE_ADMINISTRATION)
    String serverBaseUrl;

    public String getServerBaseUrl() {
        return serverBaseUrl;
    }

}
