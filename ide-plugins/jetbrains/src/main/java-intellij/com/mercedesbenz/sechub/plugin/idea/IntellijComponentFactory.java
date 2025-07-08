// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea;

import javax.swing.*;

import com.intellij.ui.components.JBScrollPane;
import com.mercedesbenz.sechub.plugin.ui.ComponentBuilder;

public class IntellijComponentFactory implements ComponentBuilder {
    @Override
    public JScrollPane createScrollPane(JComponent component) {
        return new JBScrollPane(component);
    }
}
