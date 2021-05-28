// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.Necessarity;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.TemplateData;

public class KeyValueUI implements TemplateDataUIPart {

    private KeyValuePanel panel;
    private JTextArea textArea;
    private TemplateData data;

    KeyValueUI(TemplateData data) {
        this.panel = new KeyValuePanel(new BorderLayout());
        this.data = data;

        JPanel descriptionPanel = new JPanel(new BorderLayout());

        String text = "<html><body>\n";
        text += "<b><u>Description:</u></b><br>" + (data.description == null ? "<no description available>" : data.description);
        text += "<br><br>";
        text += createNecessarityHTML(data);
        text += " <i>(Type:</b> " + data.type + ")</i>";
        text += "</body></html>";

        JEditorPane descriptionTextArea = new JEditorPane();
        descriptionTextArea.setContentType("text/html");
        descriptionTextArea.setText(text);
        descriptionTextArea.setEditable(false);

        descriptionPanel.add(new JScrollPane(descriptionTextArea), BorderLayout.CENTER);

        textArea = new JTextArea();
        
        /* when recommended, we provide the recommended value initially - will be overridden when defined */
        if (data.necessarity == Necessarity.RECOMMENDED) {
            if (data.recommendedValue != null || !data.recommendedValue.isEmpty()) {
                textArea.setText(data.recommendedValue);
            }

        }
        JSplitPane splitPane = new JSplitPane(0, descriptionPanel, textArea);

        this.panel.add(splitPane, BorderLayout.CENTER);
    }

    private String createNecessarityHTML(TemplateData data) {
        String html = "";
        html += "Necessarity:<b><span";

        if (data.necessarity.equals(Necessarity.MANDATORY)) {
            html += " style='color:red'";
        } else if (data.necessarity.equals(Necessarity.UNKNOWN)) {
            html += " style='color:orange'";
        } else if (data.necessarity.equals(Necessarity.RECOMMENDED)) {
            html += " style='color:blue'";
        }
        html += ">";
        html += data.necessarity;
        if (data.necessarity.equals(Necessarity.RECOMMENDED)) {
            html +="=>"+data.recommendedValue;
        }
        html += "</span></b>";
        return html;
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
