// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
import com.mercedesbenz.sechub.commons.model.template.TemplateDataResolver;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;

@Component
/**
 * Filters template definitions which are relevant for a scan
 *
 * @author Albert Tregnaghi
 *
 */
public class RelevantScanTemplateDefinitionFilter {

    @Autowired
    TemplateDataResolver templateDataResolver;

    /**
     * Filters template definitions for only relevant definitions for given scan
     * type and configuration. If the configuration does not contain a templateData
     * definition, the list will always be empty. If configuration contains template
     * data, but the scan type does not need/use them the result will also be empty
     *
     * @param templateDefinitions all given definitions
     * @param scanType            scan type for which relevant definitions shall be
     *                            filtered
     * @param configuration       SecHub configuration which contains template data
     *                            (or not)
     * @return list of relevant template definitions for the scan, never
     *         <code>null</code>
     */
    public List<TemplateDefinition> filter(List<TemplateDefinition> templateDefinitions, ScanType scanType, SecHubConfigurationModel configuration) {
        List<TemplateDefinition> result = new ArrayList<>();

        for (TemplateDefinition definition : templateDefinitions) {

            addDefinitionToResultIfNecessary(definition, scanType, configuration, result);
        }
        return result;
    }

    private void addDefinitionToResultIfNecessary(TemplateDefinition definition, ScanType scanType, SecHubConfigurationModel configuration,
            List<TemplateDefinition> result) {
        if (scanType == null) {
            return;
        }
        switch (scanType) {
        case WEB_SCAN:
            /* for web scan we accept WEBSCAN_LOGIN templates */
            if (TemplateType.WEBSCAN_LOGIN.equals(definition.getType())) {
                TemplateData templateData = templateDataResolver.resolveTemplateData(TemplateType.WEBSCAN_LOGIN, configuration);
                if (templateData != null) {
                    /* data available, so add to result */
                    result.add(definition);
                }
            }
            break;
        default:
            break;
        }

    }

}
