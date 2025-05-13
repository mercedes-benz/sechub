package com.mercedesbenz.sechub.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;

@Configuration
public class SecHubJSONMapperConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        /*
         * This is a custom ObjectMapper bean that is used for JSON serialization and
         * deserialization. Be aware that this does not apply to custom PathVariables
         * and PathParameters, which should be handled by custom converters.
         */
        return JsonMapperFactory.createMapper();
    }
}
