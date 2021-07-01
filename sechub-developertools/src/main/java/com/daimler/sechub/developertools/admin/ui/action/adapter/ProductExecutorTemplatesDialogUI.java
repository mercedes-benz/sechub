// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.adapter.MappingUI.MappingPanel;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.Necessarity;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.TemplateData;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.Type;
import com.daimler.sechub.developertools.admin.ui.util.SortedMapToTextConverter;
import com.daimler.sechub.developertools.admin.ui.util.TextToSortedMapConverter;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;

public class ProductExecutorTemplatesDialogUI {

    private UIContext context;
    private String productId;
    private JPanel mappingPanel;
    private TemplatesDialogData dialogData;
    private int version;
    private JTabbedPane mappingIdTabPane;
    private JPanel keyValuesPanel;
    private JTabbedPane keyValueTabPane;
    private JTabbedPane mainTabPane;
    private ImportFromClipboardAction importFromClipboardAction;
    private JDialog dialog;
    private TemplatesDialogResult result;

    public ProductExecutorTemplatesDialogUI(UIContext context, ProductIdentifier productId, int version, TemplatesDialogData data) {
        this.context = context;
        this.productId = productId.name();
        this.dialogData = data;
        this.version = version;
    }

    UIContext getContext() {
        return context;
    }

    public class TemplatesDialogResult {
        public boolean provideExportAllButtonPressed;
        public String outputContent;
    }

    public static class TemplatesDialogConfig {
        public boolean provideExportAllButton;
        public String provideExportAllButtonText = "Export all";
        public String inputContent;
    }

    public TemplatesDialogResult showDialog(TemplatesDialogConfig config) {
        dialog = new JDialog(context.getFrame());
        dialog.setLayout(new BorderLayout());

        mainTabPane = new JTabbedPane();

        mappingPanel = new JPanel(new BorderLayout());
        keyValuesPanel = new JPanel(new BorderLayout());

        mainTabPane.add("mapping", mappingPanel);
        mainTabPane.add("key/values", keyValuesPanel);

        mainTabPane.setSelectedComponent(mappingPanel);

        fillMappingPanel();
        fillKeyValuesPanel();

        dialog.add(mainTabPane, BorderLayout.CENTER);

        JMenuBar menuBar = createMainMenu();

        if (config.inputContent != null) {
            /* we use the importer all time to initialize content */
            SwingUtilities.invokeLater(() -> importFromClipboardAction.importText(config.inputContent));
        }else {
            /* when new at all */
            System.out.println("No input content found!");
        }

        if (config.provideExportAllButton) {
            JButton button = new JButton(new ExportAllToResultOutputContentAction(config.provideExportAllButtonText));
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(button);

            dialog.add(buttonPanel, BorderLayout.SOUTH);
        }
        result = new TemplatesDialogResult();

        dialog.setJMenuBar(menuBar);
        dialog.setTitle("Product executor:" + productId + " ,version:" + version);
        dialog.setModal(true);
        dialog.setSize(new Dimension(1024, 600));
        dialog.setLocationRelativeTo(context.getFrame());
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        return result;
    }

    private void fillKeyValuesPanel() {

        keyValueTabPane = new JTabbedPane();
        keyValueTabPane.setTabPlacement(JTabbedPane.LEFT);
        List<TemplateData> keyValueData = dialogData.getKeyValueData();

        for (TemplateData data : keyValueData) {

            add(data);
        }

        keyValuesPanel.add(keyValueTabPane);
    }

    private JMenuBar createMainMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu importMenu = new JMenu("Import");
        menuBar.add(importMenu);
        importFromClipboardAction = new ImportFromClipboardAction();
        importMenu.add(importFromClipboardAction);

        JMenu exportMenu = new JMenu("Export");
        menuBar.add(exportMenu);
        exportMenu.add(new ExportAllToClipboardAction());
        return menuBar;
    }

    private JMenuBar createMenuForMappingsOnly() {
        JMenuBar menuBar = new JMenuBar();
        JMenu importMenu = new JMenu("Import");
        menuBar.add(importMenu);
        importMenu.add(new ImportFromCSVAction());

        JMenu exportMenu = new JMenu("Export");
        menuBar.add(exportMenu);

        exportMenu.add(new ExportToCSVAction());
        exportMenu.add(new ExportToClipboardAction());

        JMenu otherMenu = new JMenu("Other");
        otherMenu.add(new LoadFromGlobalMappingStorageAction());
        otherMenu.add(new SaveToGlobalMappingStorageAction());
        menuBar.add(otherMenu);

        otherMenu.add(new CreateExampleAction());
        return menuBar;
    }

    private void fillMappingPanel() {

        mappingIdTabPane = new JTabbedPane();
        mappingIdTabPane.setTabPlacement(JTabbedPane.LEFT);
        mappingIdTabPane.setTabPlacement(JTabbedPane.LEFT);
        List<TemplateData> mappingData = dialogData.getMappingData();

        for (TemplateData data : mappingData) {

            add(data);
        }
        mappingPanel.add(createMenuForMappingsOnly(), BorderLayout.NORTH);
        mappingPanel.add(mappingIdTabPane);

    }

    private void updateOrCreate(TemplateData data, String value) {
        JTabbedPane tabPane = null;
        if (data.type.equals(Type.MAPPING)) {
            tabPane = mappingIdTabPane;
        } else {
            tabPane = keyValueTabPane;
        }
        boolean existed = false;
        for (int i = 0; i < tabPane.getComponentCount(); i++) {
            Component component = tabPane.getComponent(i);
            if (!(component instanceof TemplateDataUIPart)) {
                throw new IllegalStateException("Component not a TemplateDataUIPart" + component);
            }
            TemplateDataUIPart uiPart = (TemplateDataUIPart) component;
            String key = uiPart.getData().key;
            if (data.key.equals(key)) {
                TemplateDataUIPart changeableText = (TemplateDataUIPart) component;
                changeableText.setText(value);
                existed = true;
            }
        }
        if (existed) {
            return;
        }
        add(data).setText(value);
    }

    final static String PRE_HTML = "<html><p style=\"text-align: left; width: 300px";
    final static String POST_HTML = "</p></html>";

    private TemplateDataUIPart add(TemplateData data) {

        if (data.type.equals(Type.MAPPING)) {
            MappingUI ui = new MappingUI(this, data);
            mappingIdTabPane.add(preHTML(data) + data.key + POST_HTML, ui.getComponent());
            return ui;
        } else {
            KeyValueUI ui = new KeyValueUI(data);
            keyValueTabPane.add(preHTML(data) + data.key + POST_HTML, ui.getComponent());
            return ui;
        }
    }

    private String preHTML(TemplateData data) {
        String additional = "";
        if (data.necessarity.equals(Necessarity.MANDATORY)) {
            additional = ";color:red";
        } else if (data.necessarity.equals(Necessarity.UNKNOWN)) {
            additional = ";color:orange";
        } else if (data.necessarity.equals(Necessarity.RECOMMENDED)) {
            additional = ";color:blue";
        }
        return PRE_HTML + additional + "\">";
    }

    private class ExportAllToResultOutputContentAction extends ExportAllAction {

        private static final long serialVersionUID = 1L;

        public ExportAllToResultOutputContentAction(String text) {
            super(text);
        }

        @Override
        protected void handleActionAfterFetchedValuesAsOneString(String content) {
            result.outputContent = content;
            result.provideExportAllButtonPressed = true;

            dialog.dispose();
        }

    }

    private abstract class AbstractMappingUIAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        protected AbstractMappingUIAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Component component = mappingIdTabPane.getSelectedComponent();
            if (component instanceof MappingPanel) {
                actionPerformed(e, ((MappingPanel) component).getMappingUI());
            }
        }

        protected abstract void actionPerformed(ActionEvent e, MappingUI ui);

    }

    private class ImportFromCSVAction extends AbstractMappingUIAction {

        private static final long serialVersionUID = 1L;

        public ImportFromCSVAction() {
            super("Import from CSV");
        }

        @Override
        protected void actionPerformed(ActionEvent e, MappingUI ui) {
            ui.getImportCSVAction().actionPerformed(e);
        }

    }

    private class LoadFromGlobalMappingStorageAction extends AbstractMappingUIAction {

        private static final long serialVersionUID = 1L;

        public LoadFromGlobalMappingStorageAction() {
            super("Load from global mapping");
        }

        @Override
        protected void actionPerformed(ActionEvent e, MappingUI ui) {
            ui.getLoadAction().actionPerformed(e);
        }

    }

    private class SaveToGlobalMappingStorageAction extends AbstractMappingUIAction {

        private static final long serialVersionUID = 1L;

        public SaveToGlobalMappingStorageAction() {
            super("Save as global mapping");
        }

        @Override
        protected void actionPerformed(ActionEvent e, MappingUI ui) {
            ui.getSaveAction().actionPerformed(e);
        }

    }

    private class ExportToCSVAction extends AbstractMappingUIAction {

        private static final long serialVersionUID = 1L;

        public ExportToCSVAction() {
            super("Export to CSV");
        }

        @Override
        protected void actionPerformed(ActionEvent e, MappingUI ui) {
            ui.getExportCSVAction().actionPerformed(e);
        }

    }

    private class ExportToClipboardAction extends AbstractMappingUIAction {

        private static final long serialVersionUID = 1L;

        public ExportToClipboardAction() {
            super("Export to clipboard as one property line");
        }

        @Override
        protected void actionPerformed(ActionEvent e, MappingUI ui) {
            ui.getCopyToClipboardAsPropertyLine().actionPerformed(e);
        }

    }

    private class CreateExampleAction extends AbstractMappingUIAction {

        private static final long serialVersionUID = 1L;

        public CreateExampleAction() {
            super("Create example mapping");
        }

        @Override
        protected void actionPerformed(ActionEvent e, MappingUI ui) {
            ui.getExampleAction().actionPerformed(e);
        }

    }

    private abstract class ExportAllAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public ExportAllAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            SortedMapToTextConverter converter = new SortedMapToTextConverter();

            TreeMap<String, String> map = new TreeMap<>();

            append(map, keyValueTabPane);
            append(map, mappingIdTabPane);

            String content = converter.convertToText(map);

            handleActionAfterFetchedValuesAsOneString(content);

        }

        protected abstract void handleActionAfterFetchedValuesAsOneString(String content);

        private void append(TreeMap<String, String> map, JTabbedPane pane) {
            for (Component c : pane.getComponents()) {
                if (c instanceof TemplateDataUIPart) {
                    TemplateDataUIPart part = (TemplateDataUIPart) c;
                    String text = part.getText();
                    String compressedJsonOrText = JSONDeveloperHelper.INSTANCE.compress(text);
                    String key = part.getData().key;
                    map.put(key, compressedJsonOrText);
                }
            }
        }

    }

    private class ExportAllToClipboardAction extends ExportAllAction {

        private static final long serialVersionUID = 1L;

        public ExportAllToClipboardAction() {
            super("Export all to clipboard");
        }

        protected void handleActionAfterFetchedValuesAsOneString(String content) {
            StringSelection selection = new StringSelection(content);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        }

    }

    private class ImportFromClipboardAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ImportFromClipboardAction() {
            super("Import from clipboard");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {

                String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

                importText(data);

            } catch (RuntimeException | UnsupportedFlavorException | IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(getContext().getFrame(), "Was not able to fetch text from clipboard");
            }
        }

        public void importText(String data) {
            TextToSortedMapConverter converter = new TextToSortedMapConverter();
            SortedMap<String, String> map = converter.convertFromText(data);

            Type lastType = null;
            for (String key : map.keySet()) {
                String val = map.get(key);
                String json = JSONDeveloperHelper.INSTANCE.beatuifyJSON(val);
                TemplateData templateData = dialogData.getData(key);
                if (templateData == null) {
                    templateData = new TemplateData();
                    templateData.key = key;
                    templateData.type = Type.UNKNOWN;
                    templateData.necessarity = Necessarity.UNKNOWN;
                    templateData.example = json;
                }
                updateOrCreate(templateData, val);
                lastType = templateData.type;
            }
            selectLastImport(lastType);
        }

        private void selectLastImport(Type lastType) {
            if (lastType == null) {
                return;
            }
            JComponent componentToSelect = null;
            JTabbedPane tabToSelect = null;
            if (lastType.equals(Type.MAPPING)) {
                tabToSelect = mappingIdTabPane;
                componentToSelect = mappingPanel;
            } else {
                tabToSelect = keyValueTabPane;
                componentToSelect = keyValuesPanel;
            }
            mainTabPane.setSelectedComponent(componentToSelect);
            tabToSelect.setSelectedIndex(tabToSelect.getTabCount() - 1);
        }
    }
}
