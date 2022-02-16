// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

public abstract class AbstractAdapterDialogMappingAction extends AbstractAdapterDialogAction {

    private static final long serialVersionUID = 1L;
    private MappingUI mappingUI;

    public AbstractAdapterDialogMappingAction(String text, MappingUI ui) {
        super(text, ui.getDialogUI());
        this.mappingUI = ui;
    }

    public MappingUI getMappingUI() {
        return mappingUI;
    }

}
