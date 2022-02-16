// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

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
        case WEB_SCAN:

            Optional<SecHubWebScanConfiguration> webScan = model.getWebScan();
            if (webScan.isPresent()) {
                newModel.setWebScan(webScan.get());
            } else {
                LOG.warn("The model did not contain a web scan configuration - so add new one as fallback");
                newModel.setWebScan(new SecHubWebScanConfiguration());
            }
            break;
        }

        String json = newModel.toJSON();
        return json;
    }

}
