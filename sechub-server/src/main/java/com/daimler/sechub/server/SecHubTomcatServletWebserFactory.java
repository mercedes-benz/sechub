// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

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
public class SecHubTomcatServletWebserFactory implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubTomcatServletWebserFactory.class);

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSizeAsString;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        /* @formatter:off 
          
           we let Tomcat always swallow one more megabyte than spring servlet filter does
           so we will get not SocketConnectionExceptions sometimes instad dedicated exceptions
           as defined in SecHubExceptionHandler 
           
           @formatter:on*/
        String size = maxFileSizeAsString.trim().toLowerCase();
        if (!size.endsWith("mb")) {
            LOG.error("Cannot customize tomcat swallow size automatically, because multipart size not supported:" + size);
            return;
        }
        size = size.substring(0, size.length() - 2);
        size = size.trim();

        int springUploadMaxFileSizeInMB = Integer.parseInt(size);
        int maxSwallowSize = (springUploadMaxFileSizeInMB + 1) * 1024 * 1024;

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
}