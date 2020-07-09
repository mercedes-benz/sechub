// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.JDialog;

import com.daimler.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public abstract class AbstractPDSAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public AbstractPDSAction(String title, UIContext context) {
		super(title,context);
	}

	@Override
	public final void execute(ActionEvent e) {
	    /* TODO Albert Tregnaghi, 2020-07-07: change this - we need a panel or a combobox with configs to handle this better */
		Optional<String> pdsServer = getUserInput("PDS server",InputCacheIdentifier.PDS_SERVER);
		if (!pdsServer.isPresent()) {
		    output("Canceled server");
			return;
		}
		Optional<String> pdsPort = getUserInput("PDS port",InputCacheIdentifier.PDS_PORT);
        if (!pdsPort.isPresent()) {
            output("Canceled port");
            return;
        }
        Optional<String> pdsUserId = getUserInput("PDS user",InputCacheIdentifier.PDS_USER);
        if (!pdsUserId.isPresent()) {
            output("Canceled user");
            return;
        }
        Optional<String> pdsApiToken = getUserPassword("PDS apitoken",InputCacheIdentifier.PDS_APITOKEN);
        if (!pdsApiToken.isPresent()) {
            output("Canceled apitoken");
            return;
        }
		int port = Integer.parseInt(pdsPort.get());
		
		String server = pdsServer.get();
		String userId=pdsUserId.get();
		String apiToken = pdsApiToken.get();
		
		executePDS(getContext().getAdministration().pds(server,port,userId,apiToken));
	}

    protected abstract void executePDS(PDSAdministration pds);

}