// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.SortedMap;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.adapter.MappingUI.MappingPanel;
import com.daimler.sechub.developertools.admin.ui.util.TextToSortedMapConverter;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;

public class ProductExecutorTemplatesDialogUI {

    private UIContext context;
    private String productId;
    private JPanel mappingPanel;
    private String[] mappingIdentifiers;
    private int version;
    private JTabbedPane mappingIdTabPane;

    public ProductExecutorTemplatesDialogUI(UIContext context, ProductIdentifier productId, int version, String... mappingIdentifiers) {
        this.context = context;
        this.productId = productId.name();
        this.mappingIdentifiers = mappingIdentifiers;
        this.version = version;
    }

    UIContext getContext() {
        return context;
    }

    public void showDialog() {
        JDialog dialog = new JDialog(context.getFrame());
        dialog.setLayout(new BorderLayout());

        JTabbedPane mainTabPane = new JTabbedPane();

        mappingPanel = new JPanel(new BorderLayout());

        mainTabPane.add("mapping", mappingPanel);

        mainTabPane.setSelectedComponent(mappingPanel);

        fillMappingPanel();

        dialog.add(mainTabPane, BorderLayout.CENTER);

        JMenuBar menuBar = createMenu();

        dialog.setJMenuBar(menuBar);
        dialog.setTitle("Templates for product executor:" + productId + " ,version:" + version);
        dialog.setModal(true);
        dialog.setSize(new Dimension(1024, 600));
        dialog.setLocationRelativeTo(context.getFrame());
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu importMenu = new JMenu("Import");
        menuBar.add(importMenu);
        importMenu.add(new TempImportFromClipboardAction());
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

        for (String mappingId : mappingIdentifiers) {

            add(mappingId);
        }

        mappingPanel.add(mappingIdTabPane);

    }

    private void add(String mappingId) {
        add(mappingId, "");
    }

    private MappingUI add(String mappingId, String prefix) {
        MappingUI part = new MappingUI(this, mappingId);
        mappingIdTabPane.add(prefix + part.getTitle(), part.getComponent());
        return part;
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

    private class TempImportFromClipboardAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private int clipboardCount;

        public TempImportFromClipboardAction() {
            super("Temporary from clipboard");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                clipboardCount++;

                String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

                TextToSortedMapConverter converter = new TextToSortedMapConverter();
                SortedMap<String, String> map = converter.convertFromText(data);

                for (String key : map.keySet()) {
                    String val = map.get(key);
                    String json = JSONDeveloperHelper.INSTANCE.beatuifyJSON(val);

                    add(key, "tmp_import_clipboard_" + clipboardCount + ":").setJSON(json);
                }
                mappingIdTabPane.setSelectedIndex(mappingIdTabPane.getTabCount() - 1);

            } catch (RuntimeException | UnsupportedFlavorException | IOException e1) {
                JOptionPane.showMessageDialog(getContext().getFrame(), "Was not able to fetch text from clipboard");
            }
        }

    }
}
