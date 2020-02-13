package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;

import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;

public class CreateExampleJSONAdapterDialogAction extends AbstractAdapterDialogMappingAction {

    private static final long serialVersionUID = 1L;

    public CreateExampleJSONAdapterDialogAction(MappingUI ui) {
        super("Show example", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        MappingData data = new MappingData();
        data.getEntries().add(new MappingEntry("pattern1", "replacement1", "comment1"));
        data.getEntries().add(new MappingEntry("pattern2", "replacement2", "comment2"));
        getMappingUI().setJSON(data.toJSON());

    }

}
