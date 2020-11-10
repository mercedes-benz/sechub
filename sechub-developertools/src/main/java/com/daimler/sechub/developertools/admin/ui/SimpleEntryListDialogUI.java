// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.util.ArrayList;
import java.util.List;

public class SimpleEntryListDialogUI extends AbstractListDialogUI<String>{
    
    private List<SimpleEntry> data;

    public SimpleEntryListDialogUI(UIContext context, String title,List<SimpleEntry> data) {
        super(context, title);
        this.data=data;
    }

    @Override
    protected void initializeDataForShowDialog() {
        
    }

    @Override
    protected int getSelectionColumn() {
        return 0;
    }

    @Override
    protected List<Object[]> createTableContent() {
        List<Object[]> list = new ArrayList<Object[]>();
        for (SimpleEntry entry :data) {
            list.add(new Object[] { entry.id, entry.description});
        }
        return list;
    }

    @Override
    protected List<String> createTableHeaders() {
        
        List<String> model = new ArrayList<>();
        model.add("id");//0
        model.add("description");
        return model;
        
    }

    
}
