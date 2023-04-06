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

    public void setStageWaits(boolean stageWaits) {
        this.stageWaits = stageWaits;
    }

    public boolean isStageWaits() {
        return stageWaits;
    }
}
