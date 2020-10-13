// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.daimler.sechub.developertools.admin.ui.AbstractListDialogUI;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.test.executorconfig.TestExecutorConfigList;
import com.daimler.sechub.test.executorconfig.TestExecutorConfigListEntry;

public class ListExecutorConfigurationDialogUI extends AbstractListDialogUI<UUID> {

    private TestExecutorConfigList profileList;

    public ListExecutorConfigurationDialogUI(UIContext context, String title) {
        super(context, title);
    }

    @Override
    protected List<String> createTableHeaders() {
        List<String> model = new ArrayList<>();
        model.add("Configuration name");//0
        model.add("enabled");
        model.add("uuid");
        return model;
    }

    @Override
    protected void initializeDataForShowDialog() {
        profileList = getContext().getAdministration().fetchExecutorConfigurationList();

    }

    @Override
    protected int getSelectionColumn() {
        return 2; // we use 2 for uuid
    }

    @Override
    protected List<Object[]> createTableContent() {
        List<Object[]> list = new ArrayList<Object[]>();
        for (TestExecutorConfigListEntry entry : profileList.executorConfigurations) {
            list.add(new Object[] { entry.name, entry.enabled, entry.uuid});
        }
        return list;
    }
}
