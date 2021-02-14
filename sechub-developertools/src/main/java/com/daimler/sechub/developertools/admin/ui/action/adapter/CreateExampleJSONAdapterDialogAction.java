// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;

import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;

public class CreateExampleJSONAdapterDialogAction extends AbstractAdapterDialogMappingAction {

    private static final long serialVersionUID = 1L;

    public CreateExampleJSONAdapterDialogAction(MappingUI ui) {
        super("Example", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        boolean confirmed = getDialogUI().getContext().getDialogUI().confirm("Do you really want to replace your JSON data in text area with example code?");
        if (! confirmed) {
            getDialogUI().getContext().getOutputUI().output("Canceled by user");
            
            return;
        }
        
        String exampleFound = getMappingUI().getData().example;
        
        if (exampleFound!=null) {
            getMappingUI().setJSON(exampleFound);
        }else {
            MappingData data = new MappingData();
            data.getEntries().add(new MappingEntry("pattern1", "replacement1", "comment1"));
            data.getEntries().add(new MappingEntry("pattern2", "replacement2", "comment2"));
            getMappingUI().setJSON(data.toJSON());
        }

    }

}
