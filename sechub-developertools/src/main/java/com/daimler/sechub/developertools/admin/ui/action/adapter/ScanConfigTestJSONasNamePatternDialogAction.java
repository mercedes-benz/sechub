// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Optional;

import com.daimler.sechub.domain.scan.config.DeveloperToolsScanConfigService;
import com.daimler.sechub.domain.scan.config.NamePatternIdprovider;
import com.daimler.sechub.domain.scan.config.ScanConfig;
import com.daimler.sechub.domain.scan.config.ScanMapping;
import com.daimler.sechub.domain.scan.config.ScanMappingToScanConfigTransformer;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;

public class ScanConfigTestJSONasNamePatternDialogAction extends AbstractAdapterDialogMappingAction{

    private static final long serialVersionUID = 1L;
    

    public ScanConfigTestJSONasNamePatternDialogAction(MappingUI ui) {
        super("Test mapping", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        /* convert to rows */
        String json = getMappingUI().getJSON();
        MappingIdentifier identifier = resolveMappingIdentifier();
        ScanConfig config = createScanConfig(json, identifier);
        
        DeveloperToolsScanConfigService scanConfigService = new DeveloperToolsScanConfigService();
        scanConfigService.switchConfigurationIfChanged(config);
        
        NamePatternIdprovider provider = scanConfigService.getNamePatternIdProvider(identifier);
        
        boolean ongoing = true;
        while(ongoing) {
            
            /* ui */
            Optional<String> value = getDialogUI().getContext().getDialogUI().getUserInput("Enter a name to check", "pattern");
            if (!value.isPresent()) {
                return;
            }
            String pattern = value.get();
            String id = provider.getIdForName(pattern);
            ongoing = getDialogUI().getContext().getDialogUI().confirm("Pattern '"+pattern+"' results in '"+id+"'\n\nDo you want to test another value?");
            
        }
        
    }

    private ScanConfig createScanConfig(String json, MappingIdentifier identifier) {
        MappingData mappingData = MappingData.fromString(json);
        ScanMapping mapping = new ScanMapping(identifier.getId());
        mapping.setData(mappingData.toJSON());
        
        
        ScanConfig config = new ScanMappingToScanConfigTransformer().transform(Collections.singletonList(mapping));
        return config;
    }

    private MappingIdentifier resolveMappingIdentifier() {
        String mappingId = getMappingUI().getMappingId();
        MappingIdentifier identifier = MappingIdentifier.getIdentifierOrNull(mappingId);
        if (identifier==null) {
            throw new IllegalStateException("did not found a mapping identifier for :"+mappingId);
        }
        return identifier;
    }

}
