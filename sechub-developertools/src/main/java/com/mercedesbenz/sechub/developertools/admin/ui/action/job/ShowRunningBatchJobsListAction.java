// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.job;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

/**
 * This action shows the JobInformation entries which only live while a batch
 * action is running. This is a vehicle to get state over the the complete
 * running batch job, without directly fetch data from spring batch database
 *
 * @author Albert Tregnaghi
 *
 */
public class ShowRunningBatchJobsListAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ShowRunningBatchJobsListAction(UIContext context) {
        super("Show all running batch jobs", context);
    }

    @Override
    public void execute(ActionEvent e) {
        String data = getContext().getAdministration().fetchRunningJobsList();
        outputAsBeautifiedJSONOnSuccess(data);
    }

}