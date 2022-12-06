package com.mercedesbenz.sechub.pds.job;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;

@Component
public class PDSWorkspacePreparationContextFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PDSWorkspacePreparationContextFactory.class);

    @Autowired
    SecHubConfigurationModelSupport modelSupport;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    public PDSWorkspacePreparationContext createPreparationContext(PDSJobConfigurationSupport configurationSupport) {
        if (configurationSupport == null) {
            throw new IllegalArgumentException("configuration support may not be null!");
        }

        PDSWorkspacePreparationContext preparationContext = new PDSWorkspacePreparationContext();
        String productId = configurationSupport.getProductId();
        String productSupportedDataTypesAsString = serverConfigService.getProductParameterDefaultValueOrNull(productId,
                PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES);

        SecHubConfigurationModel model = configurationSupport.resolveSecHubConfigurationModel();

        Set<SecHubDataConfigurationType> supportedDataTypes = configurationSupport.getSupportedDataTypes(productSupportedDataTypesAsString);

        LOG.debug("Found supported data types: {} for produt: {}", supportedDataTypes, configurationSupport.getProductId());

        preparationContext.setSourceAccepted(supportedDataTypes.contains(SecHubDataConfigurationType.SOURCE));
        preparationContext.setBinaryAccepted(supportedDataTypes.contains(SecHubDataConfigurationType.BINARY));
        preparationContext.setNoneAccepted(supportedDataTypes.contains(SecHubDataConfigurationType.NONE));

        if (!preparationContext.isSourceAccepted() && !preparationContext.isBinaryAccepted()) {
            /* in this case, no model check is necessary */
            return preparationContext;

        }

        if (model != null) {

            PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(productId);
            if (productSetup == null) {
                throw new IllegalStateException("PDS product setup for " + productId + " not found!");
            }
            ScanType scanType = null;
            if (productSetup != null) {
                scanType = productSetup.getScanType();
            }
            if (scanType == null) {
                throw new IllegalStateException("PDS product setup for " + productId + " has no scan type defined!");
            }

            if (preparationContext.isBinaryAccepted()) {
                preparationContext.setBinaryAccepted(modelSupport.isBinaryRequired(scanType, model));
                if (!preparationContext.isBinaryAccepted()) {
                    LOG.info("Product: {} could handle binary content, but no referenced binary content found in model for scanType: {}", productId, scanType);
                }
            }

            if (preparationContext.isSourceAccepted()) {
                preparationContext.setSourceAccepted(modelSupport.isSourceRequired(scanType, model));
                if (!preparationContext.isSourceAccepted()) {
                    LOG.info("Product: {} could handle source content, but no referenced source content found in model for scanType: {}", productId, scanType);
                }
            }
        }

        return preparationContext;
    }
}
