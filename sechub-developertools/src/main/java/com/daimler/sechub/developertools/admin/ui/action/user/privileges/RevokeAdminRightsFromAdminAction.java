// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.user.privileges;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class RevokeAdminRightsFromAdminAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public RevokeAdminRightsFromAdminAction(UIContext context) {
		super("Revoke admin rights from user",context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> userToSignup = getUserInput("Please enter userid who will have no longer admin rights",InputCacheIdentifier.USERNAME);
		if (!userToSignup.isPresent()) {
			return;
		}
		
		if (!confirm("Do you really want to revoke admin rights for user " + userToSignup.get() + "?")) {
		    return;
		}
		
		String infoMessage = getContext().getAdministration().revokeAddminRightsFrom(userToSignup.get().toLowerCase().trim());
		outputAsTextOnSuccess(infoMessage);
	}

}