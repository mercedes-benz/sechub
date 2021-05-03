// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.daimler.sechub.developertools.admin.ui.util.DataCollectorUtils;

public class ShowProjectDetailAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ShowProjectDetailAction(UIContext context) {
        super("Show project detail", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> opt = getUserInput("Please enter project ID/name", InputCacheIdentifier.PROJECT_ID);
        if (!opt.isPresent()) {
            return;
        }
        String profileId = opt.get();
        String data = getContext().getAdministration().fetchProjectInfo(asSecHubId(opt.get()));
        outputAsBeautifiedJSONOnSuccess(data);

        data = DataCollectorUtils.fetchProfileInformationAboutProject(profileId, getContext());
        outputAsTextOnSuccess(data);
    }

}