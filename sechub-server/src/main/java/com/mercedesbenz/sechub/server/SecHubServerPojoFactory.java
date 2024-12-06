// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironment;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator;
import com.mercedesbenz.sechub.commons.model.TrafficLightSupport;
import com.mercedesbenz.sechub.commons.model.template.TemplateDataResolver;

/**
 * This factory creates some "plain old java" objects and inject them into
 * spring boot container. These objects are from libraries where we do not have
 * spring annotations inside for automatic injection.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class SecHubServerPojoFactory {

    @Bean
    TrafficLightSupport createTrafficLightSupport() {
        return new TrafficLightSupport();
    }

    @Bean
    CheckSumSupport createSha256CheckSumSupport() {
        return new CheckSumSupport();
    }

    @Bean
    CodeScanPathCollector createCodeScanPathCollector() {
        return new CodeScanPathCollector();
    }

    @Bean
    SystemEnvironment createSystemEnvironment() {
        return new SystemEnvironment();
    }

    @Bean
    SystemEnvironmentVariableSupport createEnvironementVariableSupport(@Autowired SystemEnvironment systemEnvironment) {
        return new SystemEnvironmentVariableSupport(systemEnvironment);
    }

    @Bean
    SecHubConfigurationModelSupport createSecHubConfigurationModelSupport() {
        return new SecHubConfigurationModelSupport();
    }

    @Bean
    SecHubConfigurationModelValidator createSecHubConfigurationValidator() {
        return new SecHubConfigurationModelValidator();
    }

    @Bean
    TemplateDataResolver createTemplateDataResolver() {
        return new TemplateDataResolver();
    }
}
