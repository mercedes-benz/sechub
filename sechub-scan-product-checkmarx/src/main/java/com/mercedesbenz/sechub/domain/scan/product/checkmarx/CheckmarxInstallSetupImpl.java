// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.domain.scan.AbstractInstallSetup;
import com.mercedesbenz.sechub.domain.scan.TargetType;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class CheckmarxInstallSetupImpl extends AbstractInstallSetup implements CheckmarxInstallSetup {

    @Value("${sechub.adapter.checkmarx.trustall:false}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL)
    private boolean trustAllCertificatesNecessary;

    @Override
    public boolean isHavingUntrustedCertificate() {
        return trustAllCertificatesNecessary;
    }

    @Override
    public final boolean isAbleToScan(TargetType type) {
        return isCode(type);
    }

    @Override
    protected void init(ScanInfo info) {
        /* we do not care - not necessary to inspect, only code is supported */
    }

}