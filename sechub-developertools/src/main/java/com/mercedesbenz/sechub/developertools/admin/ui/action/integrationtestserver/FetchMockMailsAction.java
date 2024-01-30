// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.mercedesbenz.sechub.integrationtest.api.MockEmailEntry;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;

public class FetchMockMailsAction extends IntegrationTestAction {
    private static final long serialVersionUID = 1L;

    public FetchMockMailsAction(UIContext context) {
        super("Fetch emails", context);
    }

    @Override
    protected void executeImplAfterRestHelperSwitched(ActionEvent e) {
        Optional<String> emailAddress = getUserInput("Please enter userid to fetch mock mails", InputCacheIdentifier.EMAILADDRESS);
        if (!emailAddress.isPresent()) {
            return;
        }
        List<MockEmailEntry> data = IntegrationTestContext.get().emailAccess().getMockMailListFor(emailAddress.get());
        for (MockEmailEntry entry : data) {
            outputAsTextOnSuccess(entry.fullToString());
        }

    }

}