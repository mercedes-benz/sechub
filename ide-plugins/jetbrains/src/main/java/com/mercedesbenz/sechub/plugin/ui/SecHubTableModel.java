// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import java.io.Serial;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class SecHubTableModel extends DefaultTableModel {

    @Serial
    private static final long serialVersionUID = 1L;

    public SecHubTableModel(String... columnNames) {
        super(columnNames, 0);
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void setDataList(List<Object[]> rows) {
        removeAllRows();

        for (Object[] row : rows) {
            addRow(row);
        }
    }

    public void removeAllRows() {
        setRowCount(0); // see javadoc, this deletes all rows in one step...
    }
}
