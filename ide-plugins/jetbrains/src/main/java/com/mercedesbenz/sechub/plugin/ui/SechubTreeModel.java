// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class SechubTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;

	public SechubTreeModel() {
        super(new SecHubRootTeeNode());
    }

    @Override
    public SecHubRootTeeNode getRoot() {
        return (SecHubRootTeeNode) super.getRoot();
    }

    @Override
    public void setRoot(TreeNode root) {
        if (!(root instanceof SecHubRootTeeNode)) {
            throw new IllegalArgumentException("root node must be instanceof SecHubRootTreeNode!");
        }
        super.setRoot(root);
    }

    public void setRoot(SecHubRootTeeNode root) {
        super.setRoot(root);
    }
}
