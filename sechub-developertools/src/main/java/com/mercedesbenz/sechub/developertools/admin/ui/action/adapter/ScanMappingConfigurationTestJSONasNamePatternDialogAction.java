// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.domain.scan.config.DeveloperToolsScanMappingConfigurationService;
import com.mercedesbenz.sechub.domain.scan.config.ScanMapping;
import com.mercedesbenz.sechub.domain.scan.config.ScanMappingConfiguration;
import com.mercedesbenz.sechub.domain.scan.config.ScanMappingToScanMappingConfigurationTransformer;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingIdentifier;

public class ScanMappingConfigurationTestJSONasNamePatternDialogAction extends AbstractAdapterDialogMappingAction {

    private static final long serialVersionUID = 1L;

    public ScanMappingConfigurationTestJSONasNamePatternDialogAction(MappingUI ui) {
        super("Test mapping", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        /* convert to rows */
        String json = getMappingUI().getJSON();
        MappingIdentifier identifier = resolveMappingIdentifier();
        ScanMappingConfiguration config = createScanConfig(json, identifier);

        DeveloperToolsScanMappingConfigurationService scanMappingConfigurationService = new DeveloperToolsScanMappingConfigurationService();
        scanMappingConfigurationService.switchConfigurationIfChanged(config);

        NamePatternIdProvider provider = scanMappingConfigurationService.getNamePatternIdProvider(identifier);

        boolean ongoing = true;
        while (ongoing) {

            /* ui */
            Optional<String> value = getDialogUI().getContext().getDialogUI().getUserInput("Enter a name to check", "pattern");
            if (!value.isPresent()) {
                return;
            }
            String pattern = value.get();
            String id = provider.getIdForName(pattern);
            ongoing = getDialogUI().getContext().getDialogUI()
                    .confirm("Pattern '" + pattern + "' results in '" + id + "'\n\nDo you want to test another value?");

        }

    }

    private ScanMappingConfiguration createScanConfig(String json, MappingIdentifier identifier) {
        MappingData mappingData = MappingData.fromString(json);
        ScanMapping mapping = new ScanMapping(identifier.getId());
        mapping.setData(mappingData.toJSON());

        ScanMappingConfiguration config = new ScanMappingToScanMappingConfigurationTransformer().transform(Collections.singletonList(mapping));
        return config;
    }

    private MappingIdentifier resolveMappingIdentifier() {
        String mappingId = getMappingUI().getMappingId();
        MappingIdentifier identifier = MappingIdentifier.getIdentifierOrNull(mappingId);
        if (identifier == null) {
            throw new IllegalStateException("did not found a mapping identifier for :" + mappingId);
        }
        return identifier;
    }

}
