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

    public ProductExecutorTemplatesDialogUI(UIContext context, ProductIdentifier productId, int version, TemplatesDialogData data) {
        this.context = context;
        this.productId = productId.name();
        this.dialogData = data;
        this.version = version;
    }

    UIContext getContext() {
        return context;
    }

    public void showDialog(boolean autoImportFromClipboard) {
        JDialog dialog = new JDialog(context.getFrame());
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

        String titleInfo = "";
        if (autoImportFromClipboard) {
            SwingUtilities.invokeLater(()->importFromClipboardAction.actionPerformed(null));
            titleInfo=" - imported from clipboard";
        }
        
        dialog.setJMenuBar(menuBar);
        dialog.setTitle("Templates for product executor:" + productId + " ,version:" + version+titleInfo);
        dialog.setModal(true);
        dialog.setSize(new Dimension(1024, 600));
        dialog.setLocationRelativeTo(context.getFrame());
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
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
        menuBar.add(otherMenu);

        otherMenu.add(new CreateExampleAction());
        return menuBar;
    }

    private void fillMappingPanel() {

        mappingIdTabPane = new JTabbedPane();
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
            String key = tabPane.getTitleAt(i);
            if (data.key.equals(key)) {
                Component component = tabPane.getComponent(i);
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

    private TemplateDataUIPart add(TemplateData data) {
        return add(data, "");
    }

    private TemplateDataUIPart add(TemplateData data, String prefix) {

        if (data.type.equals(Type.MAPPING)) {
            MappingUI ui = new MappingUI(this, data);
            mappingIdTabPane.add(prefix + ui.getLabel(), ui.getComponent());
            return ui;
        } else {
            KeyValueUI ui = new KeyValueUI(data);
            keyValueTabPane.add(prefix + data.key, ui.getComponent());
            return ui;
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

    private class ExportAllToClipboardAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        
        public ExportAllToClipboardAction() {
            super("Export all to clipboard");
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            SortedMapToTextConverter converter = new SortedMapToTextConverter();

            TreeMap<String, String> map = new TreeMap<>();

            append(map, keyValueTabPane);
            append(map, mappingIdTabPane);

            String content = converter.convertToText(map);

            StringSelection selection = new StringSelection(content);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);

        }

        private void append(TreeMap<String, String> map, JTabbedPane pane) {
            for (Component c: pane.getComponents()) {
                if (c instanceof TemplateDataUIPart) {
                    TemplateDataUIPart part = (TemplateDataUIPart) c;
                    String text = part.getText();
                    String compressedJsonOrText = JSONDeveloperHelper.INSTANCE.compress(text);
                    String key= part.getData().key;
                    map.put(key, compressedJsonOrText);
                }
            }
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

            } catch (RuntimeException | UnsupportedFlavorException | IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(getContext().getFrame(), "Was not able to fetch text from clipboard");
            }
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
