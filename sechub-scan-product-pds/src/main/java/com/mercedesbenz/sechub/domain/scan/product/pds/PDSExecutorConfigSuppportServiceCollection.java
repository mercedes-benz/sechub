package com.mercedesbenz.sechub.domain.scan.product.pds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.scan.config.ScanMappingConfigurationService;
import com.mercedesbenz.sechub.sharedkernel.SystemEnvironment;

@Component
public class PDSExecutorConfigSuppportServiceCollection {

    @Autowired
    SystemEnvironment systemEnvironment;

    @Autowired
    ScanMappingConfigurationService mappingConfigurationService;

    public SystemEnvironment getSystemEnvironment() {
        return systemEnvironment;
    }

    public ScanMappingConfigurationService getMappingConfigurationService() {
        return mappingConfigurationService;
    }

}
