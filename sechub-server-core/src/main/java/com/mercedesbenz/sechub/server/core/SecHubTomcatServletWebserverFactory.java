// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class SecHubTomcatServletWebserverFactory implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private static final int FACTOR_BYTES = 1;

    private static final int FACTOR_KILOBYTESS = 1024;

    private static final int FACTOR_MEGABYTES = 1024 * 1024;

    private static final Logger LOG = LoggerFactory.getLogger(SecHubTomcatServletWebserverFactory.class);

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSizeAsString;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        /* @formatter:off

           we let Tomcat always swallow one more megabyte than spring servlet filter does
           so we will get not SocketConnectionExceptions sometimes instead dedicated exceptions
           as defined in SecHubExceptionHandler

           @formatter:on*/
        int maxSwallowSize = calculateBytesForOneMegabyteMoreThan(maxFileSizeAsString);

        if (maxSwallowSize == -1) {
            LOG.error("Cannot customize tomcat swallow size automatically, because size format not supported:" + maxFileSizeAsString);
            return;
        }

        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
                    LOG.info("Set max swallow size to {}", maxSwallowSize);
                    ((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSwallowSize(maxSwallowSize);
                }
            }
        });
    }

    static int calculateBytesForOneMegabyteMoreThan(String givenSpringMaxUploadSize) {
        if (givenSpringMaxUploadSize == null || givenSpringMaxUploadSize.isEmpty()) {
            return -1;
        }
        String springUploadMaxFileSize = givenSpringMaxUploadSize.trim().toLowerCase();
        if (springUploadMaxFileSize.isEmpty()) {
            return -1;
        }

        int factor = 0;
        int remove = 0;

        if (springUploadMaxFileSize.endsWith("mb")) {
            factor = FACTOR_MEGABYTES;
            remove = 2;
        } else if (springUploadMaxFileSize.endsWith("kb")) {
            factor = FACTOR_KILOBYTESS;
            remove = 2;
        } else if (springUploadMaxFileSize.endsWith("b")) {
            factor = FACTOR_BYTES;
            remove = 1;
        } else {
            return -1;
        }
        if (springUploadMaxFileSize.length() < remove) {
            return -1;
        }
        springUploadMaxFileSize = springUploadMaxFileSize.substring(0, springUploadMaxFileSize.length() - remove);
        if (springUploadMaxFileSize.isEmpty()) {
            return -1;
        }
        springUploadMaxFileSize = springUploadMaxFileSize.trim();

        int springUploadMaxFileSizeAsInt = Integer.parseInt(springUploadMaxFileSize);
        int maxSwallowSize = (springUploadMaxFileSizeAsInt) * factor + (1 * FACTOR_MEGABYTES);

        return maxSwallowSize;
    }
}