// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.infralight.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.wrapper.infralight.cli.InfralightWrapperEnvironment;

@Component
public class InfralightWrapperPDSUserMessageSupportFactory {

    @Autowired
    InfralightWrapperEnvironment environment;

    @Autowired
    TextFileWriter writer;

    @Bean
    PDSUserMessageSupport createUserMessageSupport() {
        String usermessageFolder = environment.getPdsUserMessagesFolder();
        PDSUserMessageSupport support = new PDSUserMessageSupport(usermessageFolder, writer);
        return support;
    }

}
