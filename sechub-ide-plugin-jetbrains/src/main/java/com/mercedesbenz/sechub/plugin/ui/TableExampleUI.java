// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import com.intellij.ui.JBColor;

public class TableExampleUI {

    private static final List<Object[]> data = new ArrayList<>();

    static {
        data.add(new Object[] { "Y", "N" });
        data.add(new Object[] { "N", "Y" });
        data.add(new Object[] { "Y", "N" });

    }

    public static void main(String[] args) {
        Runnable r = new Runnable() {
            private SecHubTableModel dataModel;

            @Override
            public void run() {

                JFrame frame = new JFrame();
                JTable // table = new JTable(data, columnName);
                table = new JTable();
                dataModel = new SecHubTableModel("Col1", "Col2");
                table.setModel(dataModel);
                TableRowSorter<SecHubTableModel> rowSorter = new TableRowSorter<>(dataModel);
                table.setRowSorter(rowSorter);

                table.getColumn("Col1").setCellRenderer(new CustomRenderer());

                initTableModel(table);

                frame.add(new JScrollPane(table));
                frame.setTitle("Rendering in JTable");
                frame.pack();
                frame.setVisible(true);
            }

            private void initTableModel(JTable table) {
                dataModel.setDataList(data);
            }
        };

        EventQueue.invokeLater(r);
    }

}

class CustomRenderer extends DefaultTableCellRenderer {
    @Serial
    private static final long serialVersionUID = 6703872492730589499L;

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (table.getValueAt(row, column).equals("Y")) {
            cellComponent.setBackground(JBColor.YELLOW);
        } else if (table.getValueAt(row, column).equals("N")) {
            cellComponent.setBackground(JBColor.GRAY);
        }
        return cellComponent;
    }
}