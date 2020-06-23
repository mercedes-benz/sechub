// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class DeclineUserSignupAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public DeclineUserSignupAction(UIContext context) {
		super("Decline user sign up",context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> userToSignup = getUserInput("Please enter userid of waiting user to decline",InputCacheIdentifier.USERNAME);
		if (!userToSignup.isPresent()) {
			return;
		}
		
		if (!confirm("Do you really want to decline the sign up request from: " + userToSignup.get() + "?")) {
		    return;
		}
		
		String infoMessage = getContext().getAdministration().declineSignup(userToSignup.get().toLowerCase().trim());
		outputAsTextOnSuccess(infoMessage);
	}

}