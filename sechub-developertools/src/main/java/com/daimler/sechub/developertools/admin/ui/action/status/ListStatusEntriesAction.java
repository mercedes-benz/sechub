// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.status;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ListStatusEntriesAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public ListStatusEntriesAction(UIContext context) {
		super("List status entries",context);
	}

	@Override
	public void execute(ActionEvent e) {
		String data = getContext().getAdministration().getStatusList();
		outputAsBeautifiedJSON(data);
	}

}