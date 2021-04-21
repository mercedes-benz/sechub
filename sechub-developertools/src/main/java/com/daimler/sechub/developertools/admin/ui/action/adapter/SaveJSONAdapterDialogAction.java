// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;
import com.daimler.sechub.sharedkernel.mapping.MappingData;

public class SaveJSONAdapterDialogAction extends AbstractAdapterDialogMappingAction{

    private static final long serialVersionUID = 1L;

    public SaveJSONAdapterDialogAction(MappingUI ui) {
        super("Save", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        DeveloperAdministration adm = getDialogUI().getContext().getAdministration();
        String url = adm.getUrlBuilder().buildUpdateMapping(getMappingUI().getMappingId());
        String json= getMappingUI().getJSON();
        
        // just check json correct
        MappingData data = MappingData.fromString(json);
        int size = data.getEntries().size();
        boolean confirmed = getDialogUI().getContext().getDialogUI().confirm("Do you really want to upload?\n\n"+size+" entries will be set!");
        if (!confirmed) {
            getDialogUI().getContext().getOutputUI().output(getClass().getSimpleName()+":Canceled by user");
            return;
        }
        
        
        adm.getRestHelper().putJSON(url, json);
        
        getDialogUI().getContext().getOutputUI().output("Updated mapping:"+getMappingUI().getMappingId());
        
    }

}
