// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironment;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;

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
}
