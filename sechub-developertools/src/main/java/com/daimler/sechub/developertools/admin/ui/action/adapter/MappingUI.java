// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.ui.action.ActionSupport;

public class MappingUI {
    private String mappingId;
    private MappingPanel panel;
    private JTextArea textArea;
    private ProductExecutorTemplatesDialogUI dialogUI;
    private LoadJSONAdapterDialogAction loadAction;
    private SaveJSONAdapterDialogAction saveAction;
    private ScanConfigTestJSONasNamePatternDialogAction testAction;
    private CopyToClipboardAsPropertyEntryAction copyToClipboardAsPropertyLine;
    private CreateExampleJSONAdapterDialogAction exampleAction;
    private ImportCSVToJSONAdapterDialogAction importCSVAction;
    private ExportJSONToCSVAdapterDialogAction exportCSVAction;

    MappingUI(ProductExecutorTemplatesDialogUI ui, String mappingId) {

        this.dialogUI = ui;
        this.mappingId = mappingId;
        this.panel = new MappingPanel(new BorderLayout());
        this.textArea = new JTextArea();

        ActionSupport.getInstance().installAllTextActionsAsPopupTo(textArea);

        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        loadAction = new LoadJSONAdapterDialogAction(this);
        saveAction = new SaveJSONAdapterDialogAction(this);
        testAction = new ScanConfigTestJSONasNamePatternDialogAction(this);
        copyToClipboardAsPropertyLine = new CopyToClipboardAsPropertyEntryAction(this);
        exampleAction = new CreateExampleJSONAdapterDialogAction(this);
        importCSVAction = new ImportCSVToJSONAdapterDialogAction(this);
        exportCSVAction = new ExportJSONToCSVAdapterDialogAction(this);

        JPanel buttonPanel = createButtonPanel();

        panel.add(buttonPanel, BorderLayout.SOUTH);

    }

    public CreateExampleJSONAdapterDialogAction getExampleAction() {
        return exampleAction;
    }

    public ImportCSVToJSONAdapterDialogAction getImportCSVAction() {
        return importCSVAction;
    }

    public ExportJSONToCSVAdapterDialogAction getExportCSVAction() {
        return exportCSVAction;
    }

    public CopyToClipboardAsPropertyEntryAction getCopyToClipboardAsPropertyLine() {
        return copyToClipboardAsPropertyLine;
    }

    public LoadJSONAdapterDialogAction getLoadAction() {
        return loadAction;
    }

    public SaveJSONAdapterDialogAction getSaveAction() {
        return saveAction;
    }
    
    public ScanConfigTestJSONasNamePatternDialogAction getTestAction() {
        return testAction;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton(loadAction));
        buttonPanel.add(new JButton(saveAction));
        buttonPanel.add(new JSeparator());
        buttonPanel.add(new JButton(testAction));
        buttonPanel.add(new JButton(copyToClipboardAsPropertyLine));
        return buttonPanel;
    }

    public String getTitle() {
        return mappingId;
    }

    public MappingPanel getComponent() {
        return panel;
    }

    ProductExecutorTemplatesDialogUI getDialogUI() {
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

    public class MappingPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        public MappingPanel(LayoutManager layoutManager) {
            super(layoutManager);
        }

        public MappingUI getMappingUI() {
            return MappingUI.this;
        }
    }
}