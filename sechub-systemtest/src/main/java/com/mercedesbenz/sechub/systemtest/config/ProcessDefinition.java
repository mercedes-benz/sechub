// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.concurrent.TimeUnit;

public class ProcessDefinition extends AbstractDefinition {

    private TimeUnitDefinition timeOut = new TimeUnitDefinition(5, TimeUnit.MINUTES);
    private boolean stageWaits;

    public TimeUnitDefinition getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(TimeUnitDefinition timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * Use this method to mark that the current stage will wait for this process to
     * end. Until the process has not ended the switch to next stage is blocked.
     *
     * @param stageWaits when <code>true</code> the stage switch is blocked until
     *                   process has ended
     */
    public void setStageWaits(boolean stageWaits) {
        this.stageWaits = stageWaits;
    }

    public boolean isStageWaits() {
        return stageWaits;
    }
}
