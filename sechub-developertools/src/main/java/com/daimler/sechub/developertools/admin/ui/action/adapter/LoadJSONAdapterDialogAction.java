// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;

public class LoadJSONAdapterDialogAction extends AbstractAdapterDialogMappingAction {

    private static final long serialVersionUID = 1L;

    public LoadJSONAdapterDialogAction(MappingUI ui) {
        super("Load", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        DeveloperAdministration adm = getDialogUI().getContext().getAdministration();
        String url = adm.getUrlBuilder().buildGetMapping(getMappingUI().getMappingId());
        String json = adm.getRestHelper().getJSON(url);
        getMappingUI().setJSON(json);

    }

}
