// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.domain.scan.config.ScanMappingRepository;

@Component
public class PDSExecutorConfigSuppportServiceCollection {

    @Autowired
    SystemEnvironmentVariableSupport systemEnvironment;

    @Autowired
    ScanMappingRepository scanMappingRepository;

    @Autowired
    PDSTemplateMetaDataService templateMetaDataService;

    public SystemEnvironmentVariableSupport getSystemEnvironmentVariableSupport() {
        return systemEnvironment;
    }

    public ScanMappingRepository getScanMappingRepository() {
        return scanMappingRepository;
    }

    public PDSTemplateMetaDataService getTemplateMetaDataService() {
        return templateMetaDataService;
    }
}
