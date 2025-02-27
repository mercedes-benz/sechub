// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;

import jakarta.annotation.PostConstruct;

/**
 * This component injects some special SecHub Spring Boot values into
 * corresponding JVM system properties. So we can configure those parts in our
 * `application.yaml` file.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class SecHubSystemPropertyInjector {

    @MustBeDocumented(value = "Define diffie hellman key length, see https://github.com/mercedes-benz/sechub/issues/689 for details", scope = DocumentationScopeConstants.SCOPE_SECURITY)
    @Value("${sechub.security.diffiehellman.length}")
    private String diffieHellmanLength;

    @PostConstruct
    public void setDiffieHellmanLength() {
        System.setProperty("jdk.tls.ephemeralDHKeySize", diffieHellmanLength);
    }
}
