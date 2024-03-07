// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecHubConfigurationModelReducedCloningSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubConfigurationModelReducedCloningSupport.class);

    public static SecHubConfigurationModelReducedCloningSupport DEFAULT = new SecHubConfigurationModelReducedCloningSupport();

    /**
     * Reduces configuration parts to only relevant information. E.g. when
     * {@link ScanType#WEB_SCAN} is set as parameter and the web configuration
     * contains also codeScan data, these data will NOT be inside the returned
     * cloned config but only the global and the {@link ScanType#WEB_SCAN} parts!
     *
     * @param model
     * @param scanTypeForClone
     * @return JSON representing a reduced {@link SecHubScanConfiguration}
     */
    public String createReducedScanConfigurationCloneJSON(SecHubConfigurationModel model, ScanType scanTypeForClone) {
        if (ScanType.ANALYTICS.equals(scanTypeForClone) || ScanType.PREPARE.equals(scanTypeForClone)) {
            /*
             * special case: for analytics and prepare phase we want always the complete
             * model, so we just return JSON for the origin model
             */
            return JSONConverter.get().toJSON(model);
        }

        SecHubScanConfiguration newModel = new SecHubScanConfiguration();
        newModel.setApiVersion(model.getApiVersion());
        newModel.setProjectId(model.getProjectId());

        switch (scanTypeForClone) {

        case CODE_SCAN:

            Optional<SecHubCodeScanConfiguration> codeScan = model.getCodeScan();
            if (codeScan.isPresent()) {
                newModel.setCodeScan(codeScan.get());
            } else {
                LOG.warn("The model did not contain a code scan configuration - so add new one as fallback");
                newModel.setCodeScan(new SecHubCodeScanConfiguration());
            }
            break;
        case INFRA_SCAN:

            Optional<SecHubInfrastructureScanConfiguration> infraScan = model.getInfraScan();
            if (infraScan.isPresent()) {
                newModel.setInfraScan(infraScan.get());
            } else {
                LOG.warn("The model did not contain a infra scan configuration - so add new one as fallback");
                newModel.setInfraScan(new SecHubInfrastructureScanConfiguration());
            }
            break;

        case LICENSE_SCAN:
            Optional<SecHubLicenseScanConfiguration> licenseScan = model.getLicenseScan();
            if (licenseScan.isPresent()) {
                newModel.setLicenseScan(licenseScan.get());
            } else {
                LOG.warn("The model did not contain a license scan configuration - so add new one as fallback");
                newModel.setLicenseScan(new SecHubLicenseScanConfiguration());
            }
            break;
        case SECRET_SCAN:
            Optional<SecHubSecretScanConfiguration> secretScan = model.getSecretScan();
            if (secretScan.isPresent()) {
                newModel.setSecretScan(secretScan.get());
            } else {
                LOG.warn("The model did not contain a secret scan configuration - so add new one as fallback");
                newModel.setSecretScan(new SecHubSecretScanConfiguration());
            }
            break;
        case WEB_SCAN:

            Optional<SecHubWebScanConfiguration> webScan = model.getWebScan();
            if (webScan.isPresent()) {
                newModel.setWebScan(webScan.get());
            } else {
                LOG.warn("The model did not contain a web scan configuration - so add new one as fallback");
                newModel.setWebScan(new SecHubWebScanConfiguration());
            }
            break;
        default:
            LOG.warn("For scan type {} we have no reduced clone implementation. So the created model does not have any content!", scanTypeForClone);
            break;
        }
        Optional<SecHubDataConfiguration> dataOpt = model.getData();
        if (dataOpt.isPresent()) {
            newModel.setData(dataOpt.get());
        }
        String json = newModel.toJSON();
        return json;
    }

}
