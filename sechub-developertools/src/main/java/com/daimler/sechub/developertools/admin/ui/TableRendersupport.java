// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class TableRendersupport {

    public void addStandardTableCellRender(JTable table) {
        TableModel model = table.getModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            table.setDefaultRenderer(model.getColumnClass(i), new SimpleBooleanRedGreenTableCellRenderer());
        }
    }

    private class SimpleBooleanRedGreenTableCellRenderer extends DefaultTableCellRenderer {

        private JComponent componentTrue;
        private JComponent componentFalse= new JTextField("off");
        private static final long serialVersionUID = 1L;
        
        public SimpleBooleanRedGreenTableCellRenderer() {
            componentTrue = createBooleanComponent(true);
            componentFalse= createBooleanComponent(false);
        }
        
        private JComponent createBooleanComponent(boolean enabled) {
            JTextField component = new JTextField(enabled ? "on": "off");
            component.setForeground(enabled ?new Color(12,155,12):Color.RED);
            component.setHorizontalAlignment(JTextField.CENTER);
            component.setEditable(false);
            component.setBorder(null);
            return component;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table , value, isSelected, hasFocus, row, column);
            if (value instanceof Boolean) {
                Component c2 = null;
                boolean enabled = (Boolean) value;
                if (enabled) {
                    c2= componentTrue;
                } else {
                    c2=componentFalse;
                }
                c2.setBackground(c.getBackground());
                return c2;
            }
            return c;
        }
    }
}
