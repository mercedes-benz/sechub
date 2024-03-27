// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.RemoteCredentialContainerFactory;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

@Component
public class PrepareWrapperPojoFactory {

    @Autowired
    PrepareWrapperEnvironment environment;

    @Autowired
    TextFileWriter writer;

    @Bean
    RemoteCredentialContainerFactory createRemoteCredentialFactory() {
        return new RemoteCredentialContainerFactory();
    }

    @Bean
    TextFileWriter createTextFilewRiter() {
        return new TextFileWriter();
    }

    @Bean
    PDSUserMessageSupport createUserMessageSupport() {
        String userMessageFolder = environment.getPdsUserMessagesFolder();
        return new PDSUserMessageSupport(userMessageFolder, writer);
    }
}
