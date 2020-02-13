package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import com.daimler.sechub.developertools.JSONDeveloperHelper;

public class MappingUI {
    private String mappingId;
    private JPanel panel;
    private JTextArea textArea;
    private AdapterDialogUI dialogUI;

    MappingUI(AdapterDialogUI ui, String mappingId) {
        this.dialogUI = ui;
        this.mappingId = mappingId;
        this.panel = new JPanel(new BorderLayout());
        this.textArea = new JTextArea();
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        
        buttonPanel.add(new JButton(new LoadJSONAdapterDialogAction(this)));
        buttonPanel.add(new JButton(new UpdateJSONAdapterDialogAction(this)));
        buttonPanel.add(new JSeparator());
        buttonPanel.add(new JButton(new CreateExampleJSONAdapterDialogAction(this)));

        panel.add(buttonPanel,BorderLayout.SOUTH);
        
    }

    public String getTitle() {
        return mappingId;
    }

    public JPanel getComponent() {
        return panel;
    }

    AdapterDialogUI getDialogUI() {
        return dialogUI;
    }

    public String getMappingId() {
        return mappingId;
    }

    public void setJSON(String json) {
        textArea.setText(JSONDeveloperHelper.INSTANCE.beatuifyJSON(json));
    }

    public String getJSON() {
        return textArea.getText();
    }
}