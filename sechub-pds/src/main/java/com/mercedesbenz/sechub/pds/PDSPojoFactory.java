// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironment;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationTypeListParser;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;

/**
 * This factory creates some "plain old java" objects and inject them into
 * spring boot container. These objects are from libraries where we do not have
 * spring annotations inside for automatic injection.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class PDSPojoFactory {

    @Bean
    SecHubDataConfigurationTypeListParser createTypeListParser() {
        return new SecHubDataConfigurationTypeListParser();
    }

    @Bean
    CheckSumSupport createSHA256checkSumSupport() {
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
    TextFileWriter createTextFileWriter() {
        return new TextFileWriter();
    }

    @Bean
    TextFileReader createTextFileReader() {
        return new TextFileReader();
    }

    @Bean
    SecHubConfigurationModelSupport createSecHubConfigurationModelSupport() {
        return new SecHubConfigurationModelSupport();
    }

    @Bean
    PDSLogSanitizer createLogSanitizer() {
        return new PDSLogSanitizer();
    }
}
