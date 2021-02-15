// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.adapter.ProductExecutorTemplatesDialogUI.TemplatesDialogConfig;
import com.daimler.sechub.developertools.admin.ui.action.adapter.ProductExecutorTemplatesDialogUI.TemplatesDialogResult;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;

public class ShowProductExecutorTemplatesDialogAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private ProductIdentifier productIdentifier;
    private TemplatesDialogData data;
    private UIContext context;
    private int version;

    public ShowProductExecutorTemplatesDialogAction(UIContext context, ProductIdentifier productIdentifier, int version, TemplatesDialogData data) {
        super("Product executor:" + productIdentifier + ", Version:" + version);
        if (productIdentifier == null) {
            throw new IllegalArgumentException();
        }
        this.productIdentifier = productIdentifier;
        this.context = context;
        this.data = data;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public ProductIdentifier getProductIdentifier() {
        return productIdentifier;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        openDialog(new TemplatesDialogConfig());
    }

    public TemplatesDialogResult openDialog(TemplatesDialogConfig config) {
        ProductExecutorTemplatesDialogUI dialogUI = new ProductExecutorTemplatesDialogUI(context, productIdentifier, version, data);
        return dialogUI.showDialog(config);
    }

    @Override
    public int hashCode() {
        return productIdentifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ShowProductExecutorTemplatesDialogAction)) {
            return false;
        }
        ShowProductExecutorTemplatesDialogAction other = (ShowProductExecutorTemplatesDialogAction) obj;
        if (!other.productIdentifier.equals(this.productIdentifier)) {
            return false;
        }
        if (other.version != this.version) {
            return false;
        }
        return true;
    }

}