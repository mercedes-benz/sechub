// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class SecHubTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	public SecHubTableModel(String... columnNames) {
		super(columnNames, 0);
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public void setDataList(List<Object[]> rows) {
		removeAllRows();

		for (Object[] row: rows){
			addRow(row);
		}
	}

	public void removeAllRows() {
		setRowCount(0); // see javadoc, this deletes all rows in one step...
	}
}
