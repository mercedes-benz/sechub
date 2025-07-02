// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import java.io.Serial;

import javax.swing.tree.DefaultMutableTreeNode;

import com.mercedesbenz.sechub.plugin.model.FindingNode;

public class SecHubTreeNode extends DefaultMutableTreeNode {

    @Serial
    private static final long serialVersionUID = 1L;

    public SecHubTreeNode(FindingNode findingNode) {
        super(findingNode);
    }

    public FindingNode getFindingNode() {
        return (FindingNode) getUserObject();
    }
}
