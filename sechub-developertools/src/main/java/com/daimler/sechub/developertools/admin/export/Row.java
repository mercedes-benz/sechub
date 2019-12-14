// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Row {

	private List<String> columns = new ArrayList<>();

	public List<String> getUnmodifiableColumns(){
		return Collections.unmodifiableList(columns);
	}

	public static RowBuilder builder() {
		return new RowBuilder();
	}

	public static class RowBuilder{

		private Row row;

		private RowBuilder() {
			row = new Row();
		}

		public RowBuilder add(String columnValue) {
			row.columns.add("\""+columnValue+"\"");
			return this;
		}
		public RowBuilder add(int columnValue) {
			row.columns.add(""+columnValue);
			return this;
		}

		public Row build() {
			Row result = row;
			row = new Row();
			return result;
		}
	}

	private Row() {

	}

	public int columnCount() {
		return columns.size();
	}

}
