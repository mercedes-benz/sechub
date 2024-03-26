// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.factory;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.RemoteCredentialContainerFactory;

@Component
public class PrepareWrapperPojoFactory {

    @Bean
    RemoteCredentialContainerFactory createRemoteCredentialFactory() {
        return new RemoteCredentialContainerFactory();
    }

}
