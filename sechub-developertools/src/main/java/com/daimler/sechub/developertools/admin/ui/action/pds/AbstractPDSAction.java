// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCache;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public abstract class AbstractPDSAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public AbstractPDSAction(String title, UIContext context) {
		super(title,context);
	}

	@Override
	public final void execute(ActionEvent e) {
	    if (!useCacheData()) {
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
	    }
	    String server = InputCache.DEFAULT.get(InputCacheIdentifier.PDS_SERVER);
	    String userId=InputCache.DEFAULT.get(InputCacheIdentifier.PDS_USER);
	    String apiToken = InputCache.DEFAULT.get(InputCacheIdentifier.PDS_APITOKEN);
	    String portAsString = InputCache.DEFAULT.get(InputCacheIdentifier.PDS_PORT);
	    if (empty(server) || empty(userId) ||  empty(userId) || empty(userId)) {
	        getContext().getErrorHandler().handleError("Some pds connection data is empty. please configure!");
	        return;
	    }
	    int port = Integer.parseInt(portAsString);
		
		executePDS(getContext().getAdministration().pds(server,port,userId,apiToken));
	}

	private boolean empty(String string) {
        return string==null||string.isEmpty();
    }

    protected boolean useCacheData() {
	    return true;
	}
	
    protected abstract void executePDS(PDSAdministration pds);

}