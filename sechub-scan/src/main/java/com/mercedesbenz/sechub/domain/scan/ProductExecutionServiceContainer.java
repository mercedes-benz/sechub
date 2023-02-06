// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.scan.product.CodeScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.InfrastructureScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.LicenseScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.WebScanProductExecutionService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;

@Component
public class ProductExecutionServiceContainer {

    @Autowired
    private CodeScanProductExecutionService codeScanProductExecutionService;

    @Autowired
    private WebScanProductExecutionService webScanProductExecutionService;

    @Autowired
    private InfrastructureScanProductExecutionService infraScanProductExecutionService;

    @Autowired
    private LicenseScanProductExecutionService licenseScanProductExecutionService;

    @Lazy
    @Autowired
    private DomainMessageService domainMessageService;

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
}
