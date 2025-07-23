package com.mercedesbenz.sechub.wrapper.infralight.factory;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.TextFileWriter;

@Component
public class InfralightWrapperPojoFactory {

    @Bean
    TextFileWriter createTextFileWriter() {
        return new TextFileWriter();
    }

    @Bean
    TextFileReader createTextFileReader() {
        return new TextFileReader();
    }
}
