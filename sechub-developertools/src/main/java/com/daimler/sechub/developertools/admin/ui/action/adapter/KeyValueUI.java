// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.TemplateData;

public class KeyValueUI implements TemplateDataUIPart {

    private KeyValuePanel panel;
    private JTextArea textArea;
    private TemplateData data;

    KeyValueUI(TemplateData data) {
        this.panel = new KeyValuePanel(new BorderLayout());
        this.data = data;

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(new JLabel("Type: " + data.type), BorderLayout.NORTH);
        panel2.add(new JLabel("Necessarity: " + data.necessarity), BorderLayout.CENTER);
        panel2.add(new JLabel("Description: " + (data.description==null ? "<no description available>" : data.description)), BorderLayout.SOUTH);

        panel.add(panel2, BorderLayout.NORTH);

        textArea = new JTextArea();
        this.panel.add(new JScrollPane(textArea));
    }

    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void setText(String text) {
        textArea.setText(text);
    }

    @Override
    public String getText() {
        return textArea.getText();
    }

    @Override
    public TemplateData getData() {
        return data;
    }

    public class KeyValuePanel extends JPanel implements TemplateDataUIPart {

        private static final long serialVersionUID = 1L;

        public KeyValuePanel(LayoutManager layoutManager) {
            super(layoutManager);
        }

        @Override
        public void setText(String text) {
            KeyValueUI.this.setText(text);
        }

        @Override
        public String getText() {
            return KeyValueUI.this.getText();
        }

        @Override
        public TemplateData getData() {
            return KeyValueUI.this.getData();
        }

    }

}
