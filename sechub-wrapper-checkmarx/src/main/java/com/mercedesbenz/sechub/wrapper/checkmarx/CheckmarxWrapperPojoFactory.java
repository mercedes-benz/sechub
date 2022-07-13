package com.mercedesbenz.sechub.wrapper.checkmarx;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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
public class CheckmarxWrapperPojoFactory {

    @Bean
    CodeScanPathCollector createCodeScanPathCollector() {
        return new CodeScanPathCollector();
    }

}
