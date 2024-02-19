// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.domain.scan.product.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;

@Component
public class ProductExecutionServiceContainer {

    @Autowired
    private PrepareProductExecutionService prepareProductExecutionService;

    @Autowired
    private AnalyticsProductExecutionService analyticsProductExecutionService;

    @Autowired
    private CodeScanProductExecutionService codeScanProductExecutionService;

    @Autowired
    private WebScanProductExecutionService webScanProductExecutionService;

    @Autowired
    private InfrastructureScanProductExecutionService infraScanProductExecutionService;

    @Autowired
    private LicenseScanProductExecutionService licenseScanProductExecutionService;

    @Autowired
    private SecretScanProductExecutionService secretScanProductExecutionService;

    @Lazy
    @Autowired
    private DomainMessageService domainMessageService;

    public PrepareProductExecutionService getPrepareProductExecutionService() {
        return prepareProductExecutionService;
    }

    public AnalyticsProductExecutionService getAnalyticsProductExecutionService() {
        return analyticsProductExecutionService;
    }

    public CodeScanProductExecutionService getCodeScanProductExecutionService() {
        return codeScanProductExecutionService;
    }

    public WebScanProductExecutionService getWebScanProductExecutionService() {
        return webScanProductExecutionService;
    }

    public InfrastructureScanProductExecutionService getInfraScanProductExecutionService() {
        return infraScanProductExecutionService;
    }

    public LicenseScanProductExecutionService getLicenseScanProductExecutionService() {
        return licenseScanProductExecutionService;
    }

    public DomainMessageService getDomainMessageService() {
        return domainMessageService;
    }

    public SecretScanProductExecutionService getSecretScanProductExecutionService() {
        return secretScanProductExecutionService;
    }
}
