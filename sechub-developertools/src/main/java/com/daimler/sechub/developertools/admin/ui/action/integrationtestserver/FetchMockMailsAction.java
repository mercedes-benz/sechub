// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.integrationtestserver;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.daimler.sechub.integrationtest.api.MockEmailEntry;
import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;

public class FetchMockMailsAction extends IntegrationTestAction {
	private static final long serialVersionUID = 1L;

	public FetchMockMailsAction(UIContext context) {
		super("Fetch emails", context);
	}


	@Override
	protected void executeImplAfterRestHelperSwitched(ActionEvent e) {
		Optional<String> emailAdress = getUserInput("Please enter userid to fetch mock mails",InputCacheIdentifier.EMAILADRESS);
		if (!emailAdress.isPresent()) {
			return;
		}
		List<MockEmailEntry> data = IntegrationTestContext.get().emailAccess().getMockMailListFor(emailAdress.get());
		for (MockEmailEntry entry: data) {
			outputAsTextOnSuccess(entry.fullToString());
		}

	}

}