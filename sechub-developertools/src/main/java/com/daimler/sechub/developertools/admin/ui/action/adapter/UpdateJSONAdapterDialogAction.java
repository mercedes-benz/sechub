package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;

public class UpdateJSONAdapterDialogAction extends AbstractAdapterDialogMappingAction{

    private static final long serialVersionUID = 1L;

    public UpdateJSONAdapterDialogAction(MappingUI ui) {
        super("Update", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        DeveloperAdministration adm = getDialogUI().getContext().getAdministration();
        String url = adm.getUrlBuilder().buildUpdateMapping(getMappingUI().getMappingId());
        String json= getMappingUI().getJSON();
        
        adm.getRestHelper().putJSon(url, json);
        
        getDialogUI().getContext().getOutputUI().output("Updated mapping:"+getMappingUI().getMappingId());
        
    }

}
