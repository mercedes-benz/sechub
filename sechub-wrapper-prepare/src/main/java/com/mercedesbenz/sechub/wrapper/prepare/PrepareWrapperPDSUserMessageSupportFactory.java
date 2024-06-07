// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

@Component
public class PrepareWrapperPDSUserMessageSupportFactory {

    @Autowired
    PrepareWrapperEnvironment environment;

    @Autowired
    TextFileWriter writer;

    @Bean
    @Lazy // lazy, to have possibility to mock environment in spring boot tests
    PDSUserMessageSupport createUserMessageSupport() {
        String userMessageFolder = environment.getPdsUserMessagesFolder();
        return new PDSUserMessageSupport(userMessageFolder, writer);
    }
}
