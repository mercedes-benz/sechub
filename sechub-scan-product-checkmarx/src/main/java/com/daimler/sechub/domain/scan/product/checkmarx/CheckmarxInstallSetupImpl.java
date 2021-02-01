// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.checkmarx.CheckmarxConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxConstants;
import com.daimler.sechub.domain.scan.AbstractInstallSetup;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.config.ScanConfigService;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;

@Component
public class CheckmarxInstallSetupImpl extends AbstractInstallSetup implements CheckmarxInstallSetup {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxInstallSetupImpl.class);

    @Value("${sechub.adapter.checkmarx.newproject.teamid}")
    @MustBeDocumented(value = "Initial team ID. When a scan is started a and checkmarx project is still missing, "
            + "a new checkmarx project will be automatically created. "
            + "For creation a team must be defined. This value is an fallback if scan config is not set (scan config "
            + "does this in a dynamic way by inspecting project names)")
    String teamIdForNewProjects;

    @Value("${sechub.adapter.checkmarx.baseurl}")
    @MustBeDocumented(value = "Base url for checkmarx")
    private String baseURL;

    @Value("${sechub.adapter.checkmarx.userid}")
    @MustBeDocumented(value = "User id of checkmarx user", secret = true)
    private String userId;

    @Value("${sechub.adapter.checkmarx.password}")
    @MustBeDocumented(value = "Password of checkmarx user", secret = true)
    private String password;

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

    @Autowired
    ScanConfigService scanConfigService;

    @Override
    public String getBaseURL() {
        return baseURL;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getTeamIdForNewProjects(String projectId) {
        String teamId = scanConfigService.getNamePatternIdProvider(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID).getIdForName(projectId);
        if (teamId != null) {
            return teamId;
        }
        return teamIdForNewProjects;
    }

    @Override
    public Long getPresetIdForNewProjects(String projectId) {
        String id = scanConfigService.getNamePatternIdProvider(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID).getIdForName(projectId);
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException e) {
            LOG.error("Was not able to handle preset id for project {} will provide null instead", projectId);
            return null;
        }
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