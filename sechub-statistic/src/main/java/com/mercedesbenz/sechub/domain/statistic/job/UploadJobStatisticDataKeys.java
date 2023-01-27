package com.mercedesbenz.sechub.domain.statistic.job;

public enum UploadJobStatisticDataKeys implements JobStatisticDataKey {

    SIZE_IN_BYTES;

    @Override
    public String getKeyValue() {
        return name();
    }
}
