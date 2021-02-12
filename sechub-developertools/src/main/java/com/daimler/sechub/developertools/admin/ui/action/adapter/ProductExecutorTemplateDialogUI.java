// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;

public class ProductExecutorTemplateDialogUI {

    private UIContext context;
    private String productId;
    private JPanel mappingPanel;
    private String[] mappingIdentifiers;
    private int version;
    private JTabbedPane mappingIdTabPane;

    public ProductExecutorTemplateDialogUI(UIContext context, ProductIdentifier productId, int version, String... mappingIdentifiers) {
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
        JMenuBar menuBar = new JMenuBar();
        JMenu importMenu = new JMenu("Mappings");
        menuBar.add(importMenu);
        importMenu.add(new TempImportFromClipboardAction());

        dialog.setJMenuBar(menuBar);
        dialog.setTitle("Template for product executor:" + productId + " v:" + version);
        dialog.setModal(true);
        dialog.setSize(new Dimension(1024, 600));
        dialog.setLocationRelativeTo(context.getFrame());
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
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

    private class TempImportFromClipboardAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private int clipboardCount;

        public TempImportFromClipboardAction() {
            super("Add property line as mapping from clipboard");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                clipboardCount++;
                
                String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                int index = data.indexOf('=');
                if (index==-1) {
                    throw new IllegalStateException("No = found");
                }
                String key = data.substring(0,index);
                String val = data.substring(index+1);
                
                String json = JSONDeveloperHelper.INSTANCE.beatuifyJSON(val);
                
                add(key,"clipboard_"+clipboardCount+"_").setJSON(json);
                mappingIdTabPane.setSelectedIndex(mappingIdTabPane.getTabCount()-1);
                
            } catch (RuntimeException | UnsupportedFlavorException | IOException e1) {
                JOptionPane.showMessageDialog(getContext().getFrame(), "Was not able to fetch text from clipboard");
            }
        }

    }
}
