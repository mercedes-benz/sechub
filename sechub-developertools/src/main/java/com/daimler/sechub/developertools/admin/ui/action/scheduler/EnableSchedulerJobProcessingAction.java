// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.scheduler;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class EnableSchedulerJobProcessingAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public EnableSchedulerJobProcessingAction(UIContext context) {
        super("Enable scheduler job processing", context);
        setIcon(getClass().getResource("/icons/material-io/twotone_play_circle_filled_black_18dp.png"));
    }

    @Override
    public void execute(ActionEvent e) {
        if (!confirm("Do you really want to continue the processing of all jobs in the queue?")) {
            return;
        }

        String infoMessage = getContext().getAdministration().enableSchedulerJobProcessing();
        outputAsTextOnSuccess(infoMessage);
    }

}