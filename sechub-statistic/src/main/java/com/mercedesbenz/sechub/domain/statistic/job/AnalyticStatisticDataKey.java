package com.mercedesbenz.sechub.domain.statistic.job;

public enum AnalyticStatisticDataKey implements JobRunStatisticDataKey {

    ALL;

    @Override
    public String getKeyValue() {
        return name();
    }
}
