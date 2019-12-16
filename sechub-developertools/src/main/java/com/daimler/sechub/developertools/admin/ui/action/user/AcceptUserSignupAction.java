// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class AcceptUserSignupAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public AcceptUserSignupAction(UIContext context) {
		super("Accept user signup",context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> userToSignup = getUserInput("Please enter signedup userid to accept",InputCacheIdentifier.USERNAME);
		if (!userToSignup.isPresent()) {
			return;
		}
		String infoMessage = getContext().getAdministration().doSignup(userToSignup.get());
		outputAsText(infoMessage);
	}

}