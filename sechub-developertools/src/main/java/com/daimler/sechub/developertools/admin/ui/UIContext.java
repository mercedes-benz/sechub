// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;

public interface UIContext {

	OutputUI getOutputUI();

	CommandUI getCommandUI();

	CredentialUI getCredentialUI();

	GlassPaneUI getGlassPaneUI();

	DialogUI getDialogUI();

	String getApiToken();

	String getUser();

	String getServer();

	int getPort();

	DeveloperAdministration getAdministration();

}