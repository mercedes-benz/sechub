// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.daimler.sechub.developertools.admin.ui.UIContext;

public class ShowAdapterDialogAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
    private String adapterId;
    private String[] mappingIdentifiers;
    private UIContext context;

	public ShowAdapterDialogAction(UIContext context, String adapterId, String ...mappingIdentifiers) {
		super("Adapter:"+adapterId);
		this.adapterId=adapterId;
		this.context=context;
		this.mappingIdentifiers=mappingIdentifiers;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    AdapterDialogUI dialogUI = new AdapterDialogUI(context, adapterId, mappingIdentifiers);
	    dialogUI.showDialog();
	}


}