// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.ui.action.ActionSupport;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.TemplateData;

public class MappingUI implements TemplateDataUIPart {
    private MappingPanel panel;
    private TemplateData data;
    private JTextArea textArea;
    private ProductExecutorTemplatesDialogUI dialogUI;
    private LoadJSONAdapterDialogAction loadAction;
    private SaveJSONAdapterDialogAction saveAction;
    private ScanConfigTestJSONasNamePatternDialogAction testAction;
    private CopyToClipboardAsPropertyEntryAction copyToClipboardAsPropertyLine;
    private CreateExampleJSONAdapterDialogAction exampleAction;
    private ImportCSVToJSONAdapterDialogAction importCSVAction;
    private ExportJSONToCSVAdapterDialogAction exportCSVAction;

    MappingUI(ProductExecutorTemplatesDialogUI ui, TemplateData data) {

        this.dialogUI = ui;
        this.data = data;
        this.panel = new MappingPanel(new BorderLayout());
        this.textArea = new JTextArea();

        ActionSupport.getInstance().installAllTextActionsAsPopupTo(textArea);

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(new JLabel("Type:" + data.type), BorderLayout.NORTH);
        panel2.add(new JLabel("Necessarity:" + data.necessarity), BorderLayout.CENTER);
        panel2.add(new JLabel("Description:" + data.description), BorderLayout.SOUTH);

        panel.add(panel2, BorderLayout.NORTH);
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
        buttonPanel.add(new JButton(testAction));
        return buttonPanel;
    }

    public String getLabel() {
        return data.key;
    }

    public MappingPanel getComponent() {
        return panel;
    }

    ProductExecutorTemplatesDialogUI getDialogUI() {
        return dialogUI;
    }

    public void setJSON(String json) {
        textArea.setText(JSONDeveloperHelper.INSTANCE.beatuifyJSON(json));
    }

    public String getJSON() {
        return textArea.getText();
    }

    public TemplateData getData() {
        return data;
    }

    @Override
    public void setText(String text) {
        setJSON(text);
    }

    @Override
    public String getText() {
        return getJSON();
    }
    
    public String getMappingId() {
        return data.key;
    }

    public class MappingPanel extends JPanel implements TemplateDataUIPart {
        private static final long serialVersionUID = 1L;

        public MappingPanel(LayoutManager layoutManager) {
            super(layoutManager);
        }

        public MappingUI getMappingUI() {
            return MappingUI.this;

        }

        @Override
        public void setText(String text) {
            MappingUI.this.setText(text);
        }

        @Override
        public String getText() {
            return MappingUI.this.getText();
        }

        @Override
        public TemplateData getData() {
            return MappingUI.this.getData();
        }
    }

  

}