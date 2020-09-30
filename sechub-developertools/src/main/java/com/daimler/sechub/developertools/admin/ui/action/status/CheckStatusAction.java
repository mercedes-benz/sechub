// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.status;

import java.awt.event.ActionEvent;
import java.util.Map;

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.developertools.admin.ui.TrafficLightComponent;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.action.other.CheckAliveAction;
import com.daimler.sechub.developertools.admin.ui.action.scheduler.RefreshSchedulerStatusAction;

public class CheckStatusAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;
    private RefreshSchedulerStatusAction refreshSchedulerStatus;
    private ListStatusEntriesAction listStatusEntries;
    private CheckAliveAction checkAlive;

    public CheckStatusAction(UIContext context) {
        super("Check status", context);
        setIcon(getClass().getResource("/icons/material-io/twotone_autorenew_black_18dp.png"));
        tooltipUseText();
        refreshSchedulerStatus = new RefreshSchedulerStatusAction(getContext());
        listStatusEntries = new ListStatusEntriesAction(getContext());
        checkAlive = new CheckAliveAction(getContext());
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        int problemCount = 0;
        StringBuilder sb = new StringBuilder();

        try {
            checkAlive.execute(e);

            refreshSchedulerStatus.execute(e);
            listStatusEntries.execute(e);

            Map<String, String> map = listStatusEntries.getLastDataAsKeyValueMap();
            String schedulerEnabledValue = map.get("status.scheduler.enabled");
            boolean schedulerEnabled = Boolean.valueOf(schedulerEnabledValue);
            if (!schedulerEnabled) {
                sb.append("Scheduler is NOT enabled!");
                problemCount++;
            }
        } catch (Exception| AssertionError  ex) {
            problemCount++;
            sb.append("Was not able to execute checks:" + ex.getMessage());
        }

        getStatusTrafficLight().setText(" " + problemCount);
        if (sb.length() == 0) {
            getStatusTrafficLight().setToolTipText("Everything fine: scheduler is enabled, server alive");
            setStatusTrafficLight(TrafficLight.GREEN);
        } else {
            getStatusTrafficLight().setToolTipText(sb.toString());
            setStatusTrafficLight(TrafficLight.RED);
        }

    }

    private void setStatusTrafficLight(TrafficLight trafficLight) {
        getStatusTrafficLight().setTrafficLight(trafficLight);
    }

    private TrafficLightComponent getStatusTrafficLight() {
        return getContext().getCommandUI().getStatusTrafficLight();
    }

    public void checkStatusWithoutEvent() {
        try {
            execute(null);
        } catch (Exception| AssertionError e) {
            getContext().getErrorHandler().handleError("Was not able to check status:" + e.getMessage());
        }

    }

}
