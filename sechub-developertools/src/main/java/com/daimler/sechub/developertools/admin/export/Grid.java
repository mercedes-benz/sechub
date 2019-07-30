package com.daimler.sechub.developertools.admin.export;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Grid {

	private List<Row> rows =new ArrayList<>();
	private Row title;

	public Grid(Row title) {
		this.title=title;
		rows.add(title);
	}

	public void add(Row row) {
		if (title.columnCount()!=row.columnCount()) {
			throw new IllegalArgumentException("Size should be "+title.columnCount()+", but was :"+row.columnCount());
		}
		rows.add(row);
	}

	public String toCSVString() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Row> rowIt= rows.iterator();rowIt.hasNext();) {
			Row row = rowIt.next();

			Iterator<String> columnIt = row.getUnmodifiableColumns().iterator();
			while (columnIt.hasNext()) {
				String column = columnIt.next();
				sb.append(column);
				if (columnIt.hasNext()) {
					sb.append(',');
				}else {
					if (rowIt.hasNext()) {
						sb.append('\n');
					}
				}
			}
		}
		return sb.toString();
	}
}
