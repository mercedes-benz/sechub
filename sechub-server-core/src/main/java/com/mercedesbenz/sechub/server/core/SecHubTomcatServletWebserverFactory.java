// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class SecHubTomcatServletWebserverFactory implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubTomcatServletWebserverFactory.class);

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        /*
         * We handle max uploads size through Spring or custom implementations. Tomcat
         * will reject any attempt to upload a file over 2 MB by default. This instructs
         * Tomcat to swallow all uploads regardless of file size.
         */
        int maxSwallowSize = -1;

        factory.addConnectorCustomizers(connector -> {
            if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
                LOG.info("Set max swallow size to {}", maxSwallowSize);
                ((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSwallowSize(maxSwallowSize);
            } else {
                LOG.warn("Unsupported protocol handler class: {}", connector.getProtocolHandler().getClass());
                LOG.warn("Unable to set max swallow size!");
            }
        });
    }
}