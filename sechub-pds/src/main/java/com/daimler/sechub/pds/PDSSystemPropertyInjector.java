// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This component injects some special PDS Spring Boot values into corresponding
 * JVM system properties. So we can configure those parts in our
 * `application.yaml` file.
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class PDSSystemPropertyInjector {

    @PDSMustBeDocumented(value = "Define diffie hellman key length, see https://github.com/Daimler/sechub/issues/689 for details", scope = "security")
    @Value("${pds.security.diffiehellman.length}")
    private String diffieHellmanLength;

    @PostConstruct
    public void setDiffieHellmanLength() {
        System.setProperty("jdk.tls.ephemeralDHKeySize", diffieHellmanLength);
    }
}
