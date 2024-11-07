// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.mercedesbenz.sechub.domain.scan.asset.AssetDetailData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFileData;

public class ManageAssetsDialogUI {

    private JFrame frame;
    private UIContext context;
    private DefaultMutableTreeNode root;
    private JTree tree;
    private JTextArea textArea;

    public ManageAssetsDialogUI(UIContext context) {
        this.context = context;

        frame = new JFrame();
        frame.setLayout(new BorderLayout());

        createMenuBar(context);

        createToolBar(context);

        frame.setTitle("Manage assets");

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        root = new DefaultMutableTreeNode(new AssetRootElement());
        DefaultTreeModel model = new DefaultTreeModel(root);
        tree = new JTree(model);

        textArea = new JTextArea();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tree), new JScrollPane(textArea));
        frame.add(splitPane, BorderLayout.CENTER);

        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node == null) {
                        return;
                    }

                    Object userObject = node.getUserObject();
                    if (userObject instanceof AssetRootElement) {
                        refreshModel();
                        return;
                    }
                    if (userObject instanceof AssetElement) {
                        node.removeAllChildren();

                        AssetElement element = (AssetElement) userObject;
                        String assetId = element.assetId;
                        AssetDetailData detailData = context.getAdministration().fetchAssetDetails(assetId);
                        textArea.setText(detailData.toFormattedJSON());
                        int added = 0;
                        for (AssetFileData info : detailData.getFiles()) {
                            node.add(new DefaultMutableTreeNode(new AssetFileElement(assetId, info.getFileName(), info.getChecksum())));
                            added++;
                        }
                        element.info = added + " files";
                    }
                    tree.repaint();
                }
            }
        });
    }

    private void createToolBar(UIContext context) {
        JToolBar toolbar = new JToolBar();
        toolbar.add(new RefresAction(context));
        toolbar.addSeparator();
        toolbar.add(new UploadAssetFileAction(context));
        toolbar.add(new DownloadAssetFileAction(context));
        toolbar.addSeparator();
        toolbar.add(new DeleteAction(context));

        frame.add(toolbar, BorderLayout.NORTH);
    }

    private void createMenuBar(UIContext context) {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu1 = new JMenu("Actions");
        menuBar.add(menu1);
        menu1.add(new RefresAction(context));
        menu1.addSeparator();
        menu1.add(new UploadAssetFileAction(context));
        menu1.add(new DownloadAssetFileAction(context));
        menu1.addSeparator();
        menu1.add(new DeleteAction(context));

        frame.setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        new ManageAssetsDialogUI(null).show();
    }

    public void show() {
        frame.setVisible(true);
    }

    private void refreshModel() {
        root.removeAllChildren();
        List<String> assetIdentifiers = context.getAdministration().fetchAllAssetIdentifiers();
        textArea.setText("Loaded asset identifiers:\n" + assetIdentifiers);
        AssetRootElement rootElement = (AssetRootElement) root.getUserObject();
        rootElement.info = "Loaded: " + assetIdentifiers.size();

        for (String assetId : assetIdentifiers) {
            root.add(new DefaultMutableTreeNode(new AssetElement(assetId)));
        }
        tree.setModel(new DefaultTreeModel(root));
    }

    private class AssetRootElement {
        private String info = "Double click to load";

        @Override
        public String toString() {
            return "assets (" + info + ")";
        }
    }

    private class AssetElement {
        private String assetId;
        private String info = "Double click to load";

        private AssetElement(String assetId) {
            this.assetId = assetId;
        }

        @Override
        public String toString() {
            return assetId + "(" + info + ")";
        }

    }

    private class AssetFileElement {

        private String checksum;
        private String fileName;
        private String assetId;

        public AssetFileElement(String assetId, String fileName, String checksum) {
            this.assetId = assetId;
            this.fileName = fileName;
            this.checksum = checksum;
        }

        @Override
        public String toString() {
            return fileName + " (" + checksum + ")";
        }

    }

    private class RefresAction extends AbstractUIAction {

        public RefresAction(UIContext context) {
            super("Refresh", context);
        }

        private static final long serialVersionUID = 7392018849800602872L;

        @Override
        protected void execute(ActionEvent e) throws Exception {
            refreshModel();
        }

    }

    private class UploadAssetFileAction extends AbstractUIAction {

        public UploadAssetFileAction(UIContext context) {
            super("Upload", context);
        }

        private static final long serialVersionUID = 7392018849800602872L;

        @Override
        protected void execute(ActionEvent e) throws Exception {

            Optional<String> assetIdOpt = getUserInput("Select asset id", InputCacheIdentifier.ASSET_ID);
            if (assetIdOpt.isEmpty()) {
                return;
            }
            File file = getContext().getDialogUI().selectFile(null);
            if (file == null) {
                return;
            }
            String assetId = assetIdOpt.get();
            getContext().getAdministration().uploadAssetFile(assetId, file);
        }

    }

    private class DownloadAssetFileAction extends AbstractUIAction {

        public DownloadAssetFileAction(UIContext context) {
            super("Download", context);
        }

        private static final long serialVersionUID = 7392018849800602872L;

        @Override
        protected void execute(ActionEvent e) throws Exception {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            Object userObject = node.getUserObject();

            if (userObject instanceof AssetFileElement) {
                AssetFileElement assetElement = (AssetFileElement) userObject;
                File file = getContext().getAdministration().downloadAssetFile(assetElement.assetId, assetElement.fileName);
                textArea.setText("Downloaded to:\n" + file.getAbsolutePath());
            }
        }

    }

    private class DeleteAction extends AbstractUIAction {

        public DeleteAction(UIContext context) {
            super("Delete", context);
        }

        private static final long serialVersionUID = 7392018849800602872L;

        @Override
        protected void execute(ActionEvent e) throws Exception {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            Object userObject = node.getUserObject();
            if (userObject instanceof AssetElement) {
                AssetElement assetElement = (AssetElement) userObject;
                String assetId = assetElement.assetId;
                if (!getContext().getDialogUI().confirm("Do you really want to delete complete asset:" + assetId)) {
                    return;
                }
                getContext().getAdministration().deleteAsset(assetId);

            } else if (userObject instanceof AssetFileElement) {
                AssetFileElement assetElement = (AssetFileElement) userObject;
                String assetId = assetElement.assetId;
                String fileName = assetElement.fileName;

                if (!getContext().getDialogUI().confirm("Do you really want to delete file:" + fileName + " from asset:" + assetId)) {
                    return;
                }
                getContext().getAdministration().deleteAssetFile(assetId, fileName);
            }
        }

    }

}
