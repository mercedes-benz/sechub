// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class AnonymousRequestNewAPITokenUserAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public AnonymousRequestNewAPITokenUserAction(UIContext context) {
		super("Request new API token for existing user",context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String>email = getUserInput("Email of user requesting new API token",InputCacheIdentifier.EMAILADRESS);
		if (!email.isPresent()) {
			return;
		}

		String infoMessage = getContext().getAdministration().requestNewApiToken(email.get());
		outputAsTextOnSuccess(infoMessage);
	}

}