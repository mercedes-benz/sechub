// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.adapter.DefaultExecutorConfigSupport;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingIdentifier;
import com.mercedesbenz.sechub.sharedkernel.validation.Validation;

public class CheckmarxExecutorConfigSuppport extends DefaultExecutorConfigSupport {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxExecutorConfigSuppport.class);

    /**
     * Creates the configuration support and VALIDATE. This will fail when
     * configuration data is not valid (e.g. mandatory keys missing)
     *
     * @param config
     * @param systemEnvironment
     * @return support
     * @throws NotAcceptableException when configuration is not valid
     */
    public static CheckmarxExecutorConfigSuppport createSupportAndAssertConfigValid(ProductExecutorContext context,
            SystemEnvironmentVariableSupport variableSupport) {
        return new CheckmarxExecutorConfigSuppport(context, variableSupport, new CheckmarxProductExecutorMinimumConfigValidation());
    }

    private CheckmarxExecutorConfigSuppport(ProductExecutorContext context, SystemEnvironmentVariableSupport variableSupport,
            Validation<ProductExecutorConfig> validation) {
        super(context, variableSupport, validation);
    }

    public boolean isAlwaysFullScanEnabled() {
        return getParameterBooleanValue(CheckmarxExecutorConfigParameterKeys.CHECKMARX_FULLSCAN_ALWAYS);
    }

    public String getTeamIdForNewProjects(String projectId) {

        String teamId = super.getNamePatternIdProvider(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId()).getIdForName(projectId);
        if (teamId != null) {
            return teamId;
        }
        LOG.error("Was not able to handle team id for project {} will fail because not possible", projectId);
        throw new SecHubRuntimeException("Configuration failure happend! A checkmarx team id MUST be available for projects with id:" + projectId);
    }

    public Long getPresetIdForNewProjects(String projectId) {
        /* preset is an optional value and must not be configured */
        String id = super.getNamePatternIdProvider(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId(), false).getIdForName(projectId);
        if (id == null) {
            return null;
        }
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException e) {
            LOG.error("Was not able to handle preset id for project {} will provide null instead", projectId);
            return null;
        }
    }

    public String getEngineConfigurationName() {
        String configuredEngineConfigurationName = getParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_ENGINE_CONFIGURATIONNAME);

        if (SimpleStringUtils.isEmpty(configuredEngineConfigurationName)) {

            return CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME;
        }
        return configuredEngineConfigurationName;
    }

    public String getClientSecret() {
        String configuredClientSecret = getParameter(CheckmarxExecutorConfigParameterKeys.CHECKMARX_CLIENT_SECRET);

        if (SimpleStringUtils.isEmpty(configuredClientSecret)) {

            return CheckmarxConstants.DEFAULT_CLIENT_SECRET;
        }
        return configuredClientSecret;
    }

}
