// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSupport;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;

/**
 * Special actions using the integration test framework directly. Will always
 * ensure the integration test server is running. Just for convenience to have
 * directly access to scenarios and integration test server special API
 *
 * @author Albert Tregnaghi
 *
 */
public abstract class IntegrationTestAction extends AbstractUIAction {

    private static final long serialVersionUID = 1L;

    public IntegrationTestAction(String text, UIContext context) {
        super(text, context);
    }

    @Override
    protected final void execute(ActionEvent e) throws Exception {
        if (!checkIntegrationTestServerRunning()) {
            return;
        }
        if (isConfirmNecessary() && !confirm("Do you really want to execute:" + getInfo() + "?")) {
            return;
        }
        executeImplAfterRestHelperSwitched(e);
    }

    protected boolean isConfirmNecessary() {
        /* per default not necessary */
        return false;
    }

    private boolean checkIntegrationTestServerRunning() {
        IntegrationTestContext integrationTestContext = IntegrationTestContext.get();
        integrationTestContext.setHostname(getContext().getServer());
        integrationTestContext.setPort(getContext().getPort());

        String isAliveURL = integrationTestContext.getUrlBuilder().buildIntegrationTestIsAliveUrl();
        if (!Boolean.TRUE.equals(IntegrationTestSupport.fetchTestServerStatus(isAliveURL))) {
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
