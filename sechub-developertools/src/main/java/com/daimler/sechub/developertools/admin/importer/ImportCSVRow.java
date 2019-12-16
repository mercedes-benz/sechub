// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.importer;

import java.util.ArrayList;
import java.util.List;

public class ImportCSVRow {

	List<ImportCSVColumn> columns = new ArrayList<>();

	public void add(String cell) {
		ImportCSVColumn column = new ImportCSVColumn();
		column.cell=cell;
		columns.add(column);
	}
}
