package com.mercedesbenz.sechub.domain.statistic.job;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

@MustBeKeptStable("Do NOT rename the enum entries - the name is used as statistic key value!")
public enum AnalyticStatisticDataKey implements JobRunStatisticDataKey {

    ALL;

    @Override
    public String getKeyValue() {
        return name();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getKeyValue();
    }
}
