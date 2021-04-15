// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.user.privileges;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class GrantAdminRightsToUserAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public GrantAdminRightsToUserAction(UIContext context) {
		super("Grant admin rights to user",context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> userToSignup = getUserInput("Please enter userid who will gain admin rights",InputCacheIdentifier.USERNAME);
		if (!userToSignup.isPresent()) {
			return;
		}
		
		if (!confirm("Do you reall want to grant admin rights to userId " + userToSignup.get() + "?")) {
		    return;
		}
		
		String infoMessage = getContext().getAdministration().gGrantAdminRightsTo(userToSignup.get().toLowerCase().trim());
		outputAsTextOnSuccess(infoMessage);
	}

}