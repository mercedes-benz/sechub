// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import com.daimler.sechub.developertools.admin.importer.CSVRow;
import com.daimler.sechub.developertools.admin.importer.MappingDataCSVSupport;
import com.daimler.sechub.developertools.admin.importer.SimpleCSVExporter;
import com.daimler.sechub.developertools.admin.ui.ConfigurationSetup;
import com.daimler.sechub.sharedkernel.mapping.MappingData;

public class ExportJSONToCSVAdapterDialogAction extends AbstractAdapterDialogMappingAction{

    private static final long serialVersionUID = 1L;
    private static MappingDataCSVSupport csvSupport = new MappingDataCSVSupport();
    

    public ExportJSONToCSVAdapterDialogAction(MappingUI ui) {
        super("Export CSV", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        /* convert to rows */
        String json = getMappingUI().getJSON();
        List<CSVRow> rows = csvSupport.toCSVRows(MappingData.fromString(json));
        
        /* select */
        String defaultPath = ConfigurationSetup.SECHUB_MASS_OPERATION_PARENTDIRECTORY.getStringValue(System.getProperty("user.home"));
        File defaultFile = new File(defaultPath,getMappingUI().getMappingId()+".csv");
        File file = getDialogUI().getContext().getDialogUI().selectFile(defaultFile.getAbsolutePath());
        if (file == null) {
            return;
        }
        /* export */
        SimpleCSVExporter exporter = new SimpleCSVExporter();
        exporter.exportCSVFile(file, rows, 3);
        
        /* inform */
        getDialogUI().getContext().getOutputUI().output("Exported to CSV file:"+file.getAbsolutePath());
        
        
    }

}
