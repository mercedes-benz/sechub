package com.mercedesbenz.sechub.pds.execution;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;

@Component
public class PDSExecutionPojoFactory {

    @Bean
    PDSProcessAdapterFactory createPDSProcessAdapterFactory() {
        return new PDSProcessAdapterFactory();
    }
}
