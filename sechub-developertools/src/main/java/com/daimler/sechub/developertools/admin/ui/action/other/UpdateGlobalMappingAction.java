// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.other;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class UpdateGlobalMappingAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public UpdateGlobalMappingAction(UIContext context) {
        super("Update global mapping", context);
    }

    @Override
    public void execute(ActionEvent e) {

        Optional<String> mappingId = getUserInput("Please enter mapping identifier", InputCacheIdentifier.MAPPING_ID);
        if (!mappingId.isPresent()) {
            return;
        }
        
        String mappingIdentifier = mappingId.get();
        String data = getContext().getAdministration().fetchGlobalMappings(mappingIdentifier);
        data = JSONDeveloperHelper.INSTANCE.beatuifyJSON(data);
        output("Fetched mapping data for mapping:"+mappingIdentifier+":\n"+data);
        
        Optional<String> dataNew = getUserInputFromTextArea("Global Mapping:"+mappingIdentifier, data);
        if (!dataNew.isPresent()) {
            return;
        }
        getContext().getAdministration().updateGlobalMappings(mappingIdentifier,dataNew.get());
        
        outputAsTextOnSuccess("Updated mapping "+mappingIdentifier);
    }

}