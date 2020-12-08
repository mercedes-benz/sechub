// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.daimler.sechub.developertools.admin.ui.SimpleEntry;
import com.daimler.sechub.developertools.admin.ui.SimpleEntryListDialogUI;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.test.TestPDSServerConfgiuration;
import com.daimler.sechub.test.TestPDSServerProductConfig;

public abstract class AbstractCreatePDSExamplePropertiesAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public AbstractCreatePDSExamplePropertiesAction(String title, UIContext context) {
        super(title, context);
    }

    @Override
    protected final void executePDS(PDSAdministration pds) {

        CreatePDSData data = selectProductIdentifierAndCreateExampleProperties(pds);
        if (data == null) {
            return;
        }
        handleExamples(data, pds);

    }

    protected abstract void handleExamples(CreatePDSData data, PDSAdministration pds);

    public class CreatePDSData{
        public String productId;
        public String jobParametersAsString;
        public TestPDSServerConfgiuration serverConfig;
    }
    
    protected CreatePDSData selectProductIdentifierAndCreateExampleProperties(PDSAdministration pds) {
        TestPDSServerConfgiuration config = pds.fetchServerConfiguration();

        List<SimpleEntry> list = new ArrayList<>();
        for (TestPDSServerProductConfig c : config.products) {
            SimpleEntry entry = new SimpleEntry();
            entry.id = c.id;
            entry.description = c.description;
            list.add(entry);
        }
        if (list.isEmpty()) {
            getContext().getDialogUI().inform("No configured product identifiers found!");
            return null;
        }

        String selectedId = null;
        if (list.size() > 1) {
            SimpleEntryListDialogUI ui = new SimpleEntryListDialogUI(getContext(), "Select product", list);
            ui.showDialog();
            selectedId = ui.getSelectedValue();
        } else {
            selectedId = list.iterator().next().id;
        }
        if (selectedId == null) {
            return null;
        }
        CreatePDSData data = new CreatePDSData();
        data.jobParametersAsString =pds.createExampleProperitesAsString(config, selectedId);
        data.productId=selectedId;
        data.serverConfig = config;
        return data;
    }

}