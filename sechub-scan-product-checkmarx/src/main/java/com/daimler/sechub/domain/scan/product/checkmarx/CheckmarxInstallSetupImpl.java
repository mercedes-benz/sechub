// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.checkmarx.CheckmarxConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxConstants;
import com.daimler.sechub.domain.scan.AbstractInstallSetup;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Component
public class CheckmarxInstallSetupImpl extends AbstractInstallSetup implements CheckmarxInstallSetup {

    @Value("${sechub.adapter.checkmarx.engineconfiguration.name:" + CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME + "}")
    @MustBeDocumented(value = "Checkmarx engine configuration name. " + "Possible values are documented in the checkmarx REST API documentation: "
            + "https://checkmarx.atlassian.net/wiki/spaces/KC/pages/223543515/Get+All+Engine+Configurations+-+GET+sast+engineConfigurations+v8.6.0+and+up")
    private String engineConfigurationName = CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME;

    @Value("${sechub.adapter.checkmarx.clientsecret:" + CheckmarxConfig.DEFAULT_CLIENT_SECRET + "}")
    @MustBeDocumented(value = "So called 'client secret' of checkmarx. At leat at the moment this ist just a fixed default value, available in public documentation at https://checkmarx.atlassian.net/wiki/spaces/KC/pages/1187774721/Using+the+CxSAST+REST+API+v8.6.0+and+up", secret = false)
    private String clientSecret = CheckmarxConfig.DEFAULT_CLIENT_SECRET;

    @Value("${sechub.adapter.checkmarx.trustall:false}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL)
    private boolean trustAllCertificatesNecessary;

    public String getClientSecret() {
        return clientSecret;
    }

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

    @Override
    public String getEngineConfigurationName() {
        return engineConfigurationName;
    }
}