package com.mercedesbenz.sechub.wrapper.checkmarx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterV1;
import com.mercedesbenz.sechub.adapter.checkmarx.MockedCheckmarxAdapter;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironment;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProviderFactory;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperCLIEnvironment;

/**
 * This factory creates some "plain old java" objects and inject them into
 * spring boot container. These objects are from libraries where we do not have
 * spring annotations inside for automatic injection.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class CheckmarxWrapperPojoFactory {

    @Autowired
    CheckmarxWrapperCLIEnvironment environment;

    @Bean
    CheckmarxAdapter createCheckmarxAdapter() {
        if (environment.isMockingEnabled()) {
            MockedCheckmarxAdapter adapter = new MockedCheckmarxAdapter();
            return adapter;
        }
        return new CheckmarxAdapterV1();
    }

    @Bean
    NamePatternIdProviderFactory createProviderFactory() {
        return new NamePatternIdProviderFactory();
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
