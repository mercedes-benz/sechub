// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.integrationtestserver;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;

/**
 * Special actions using the integration test framework directly. Will always ensure the integration test server is running.
 * Just for convenience to have directly access to scenarios and integration test server special API
 * @author Albert Tregnaghi
 *
 */
public abstract class IntegrationTestAction extends AbstractUIAction{

	private static final long serialVersionUID = 1L;

	public IntegrationTestAction(String text, UIContext context) {
		super(text, context);
	}

	@Override
	protected final void execute(ActionEvent e) throws Exception {
		if (!checkIntegrationTestServerRunning()) {
			return;
		}
		if (isConfirmNecessary() && !confirm("Do you really want to execute:"+getInfo())) {
			return;
		}
		executeImplAfterRestHelperSwitched(e);
	}

	protected boolean isConfirmNecessary() {
		/* per default not necessary */
		return false;
	}

	private boolean checkIntegrationTestServerRunning() {
		IntegrationTestContext.get().setHostname(getContext().getServer());
		IntegrationTestContext.get().setPort(getContext().getPort());

		if (! Boolean.TRUE.equals(IntegrationTestSetup.fetchTestServerStatus())){
			warn("You are not running an integration test server, so cannot exeucte action!");
			return false;
		}
		return true;

	}

	protected String getInfo() {
		return getName();
	}

	protected abstract void executeImplAfterRestHelperSwitched(ActionEvent e);


}
