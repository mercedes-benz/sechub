// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.other;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class FetchGlobalMappingAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public FetchGlobalMappingAction(UIContext context) {
        super("Fetch global mapping", context);
    }

    @Override
    public void execute(ActionEvent e) {

        Optional<String> mappingId = getUserInput("Please enter mapping identifier", InputCacheIdentifier.MAPPING_ID);
        if (!mappingId.isPresent()) {
            return;
        }
        String mappingData = getContext().getAdministration().fetchGlobalMappings(mappingId.get());
        mappingData = JSONDeveloperHelper.INSTANCE.beatuifyJSON(mappingData);
        outputAsTextOnSuccess("mapping for id:"+mappingId.get()+" is :\n"+mappingData);
    }

}