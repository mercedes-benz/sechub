// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ColouredComboBoxRenderer<E> implements ListCellRenderer<E> {

    private JLabel label;

    private Map<E, Color> foregroundColorMap;

    private Color defaultForegroundColor;

    public ColouredComboBoxRenderer(Font font, Color defaultForegroundColor, Map<E, Color> foregroundColorMap) {
        this.defaultForegroundColor = defaultForegroundColor;
        this.foregroundColorMap = foregroundColorMap;

        label = new JLabel();
        label.setOpaque(true);
        label.setFont(font);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {

        label.setText(value.toString());
        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
        } else {
            label.setBackground(list.getBackground());
        }

        Color foreGroundColor = null;
        if (foregroundColorMap != null && !foregroundColorMap.isEmpty()) {
            foreGroundColor = foregroundColorMap.get(value);
        }
        if (foreGroundColor == null) {
            label.setForeground(defaultForegroundColor);
        } else {
            label.setForeground(foreGroundColor);
        }
        return label;
    }
}
