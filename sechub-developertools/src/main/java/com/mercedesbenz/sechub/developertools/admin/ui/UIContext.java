// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import javax.swing.JFrame;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;
import com.mercedesbenz.sechub.developertools.admin.ErrorHandler;

public interface UIContext {

    OutputUI getOutputUI();

    CommandUI getCommandUI();

    CredentialUI getCredentialUI();

    PDSConfigurationUI getPDSConfigurationUI();

    GlassPaneUI getGlassPaneUI();

    DialogUI getDialogUI();

    String getApiToken();

    String getUser();

    String getServer();

    int getPort();

    DeveloperAdministration getAdministration();

    JFrame getFrame();

    ErrorHandler getErrorHandler();

}