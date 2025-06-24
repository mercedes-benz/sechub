// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;

@Component
public class CheckmarxInstallSetupImpl implements CheckmarxInstallSetup {

    @Value("${sechub.adapter.checkmarx.trustall:false}")
    @MustBeDocumented(value = AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL, scope = DocumentationScopeConstants.SCOPE_CHECKMARX)
    private boolean trustAllCertificatesNecessary;

    @Override
    public boolean isHavingUntrustedCertificate() {
        return trustAllCertificatesNecessary;
    }

}