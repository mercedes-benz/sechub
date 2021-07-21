// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

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

    @MustBeDocumented(value = "Define diffie hellman key length, see https://github.com/Daimler/sechub/issues/689 for details", scope = "security")
    @Value("${sechub.security.diffiehellman.length}")
    private String diffieHellmanLength;

    @PostConstruct
    public void setDiffieHellmanLength() {
        System.setProperty("jdk.tls.ephemeralDHKeySize", diffieHellmanLength);
    }
}
