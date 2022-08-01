package com.mercedesbenz.sechub.wrapper.checkmarx.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperEnvironment;

@Component
public class CheckmarxWrapperPDSUserMessageSupportFactory {

    @Autowired
    CheckmarxWrapperEnvironment environment;

    @Autowired
    TextFileWriter writer;

    @Bean
    PDSUserMessageSupport createUserMessageSupport() {
        String usermessageFolder = environment.getPdsUserMessagesFolder();
        PDSUserMessageSupport support = new PDSUserMessageSupport(usermessageFolder, writer);
        return support;
    }

}
