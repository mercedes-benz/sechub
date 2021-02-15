// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.importer.CSVRow;
import com.daimler.sechub.developertools.admin.importer.MappingDataCSVSupport;
import com.daimler.sechub.developertools.admin.importer.SimpleCSVImporter;
import com.daimler.sechub.developertools.admin.ui.ConfigurationSetup;
import com.daimler.sechub.sharedkernel.mapping.MappingData;

public class ImportCSVToJSONAdapterDialogAction extends AbstractAdapterDialogMappingAction {

    private static final long serialVersionUID = 1L;
    private static MappingDataCSVSupport csvSupport = new MappingDataCSVSupport();

    public ImportCSVToJSONAdapterDialogAction(MappingUI ui) {
        super("Import CSV", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        /* select */
        String defaultPath = ConfigurationSetup.SECHUB_MASS_OPERATION_PARENTDIRECTORY.getStringValue(System.getProperty("user.home"));
        String mappingId = getMappingUI().getMappingId();
        File defaultFile = new File(defaultPath, mappingId + ".csv");
        File file = getDialogUI().getContext().getDialogUI().selectFile(defaultFile.getAbsolutePath());
        if (file == null) {
            return;
        }
        if (!file.getName().equals(defaultFile.getName())) {
            boolean confirmed = getDialogUI().getContext().getDialogUI()
                    .confirm("File name not as expected - is mappingId:" + mappingId + " really saved inside " + file.getName() + " ?");
            if (!confirmed) {
                getDialogUI().getContext().getOutputUI().output("Canceled by user");
                return;
            }
        }

        /* import */
        SimpleCSVImporter importer = new SimpleCSVImporter();
        List<CSVRow> rows = importer.importCSVFile(file, 3, 1);

        /* convert */
        MappingData data = csvSupport.fromCSVRows(rows, 0);
        String json = data.toJSON();

        /* output beautified */
        getMappingUI().setJSON(JSONDeveloperHelper.INSTANCE.beatuifyJSON(json));

        /* inform */
        getDialogUI().getContext().getOutputUI().output("Imported from CSV file:" + file.getAbsolutePath());

    }

}
