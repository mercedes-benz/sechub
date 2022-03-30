// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class CheckmarxInstallSetupImpl implements CheckmarxInstallSetup {

    @Value("${sechub.adapter.checkmarx.trustall:false}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL)
    private boolean trustAllCertificatesNecessary;

    @Override
    public boolean isHavingUntrustedCertificate() {
        return trustAllCertificatesNecessary;
    }


}