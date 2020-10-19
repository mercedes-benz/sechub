// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.developertools.admin.ui.AbstractListDialogUI;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileList;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileListEntry;

public class ListExecutionProfilesDialogUI extends AbstractListDialogUI<String> {

    private TestExecutionProfileList profileList;

    public ListExecutionProfilesDialogUI(UIContext context, String title) {
        super(context, title);
    }

    @Override
    protected List<String> createTableHeaders() {
        List<String> model = new ArrayList<>();
        model.add("Profile id");//0
        model.add("enabled");
        model.add("descrition");
        return model;
    }

    @Override
    protected void initializeDataForShowDialog() {
        profileList = getContext().getAdministration().fetchExecutionProfileList();

    }

    @Override
    protected int getSelectionColumn() {
        return 0; // we use 0 for profile -id
    }

    @Override
    protected List<Object[]> createTableContent() {
        List<Object[]> list = new ArrayList<Object[]>();
        for (TestExecutionProfileListEntry entry : profileList.executionProfiles) {
            list.add(new Object[] { entry.id, entry.enabled, entry.description });
        }
        return list;
    }

    
}
