package com.mercedesbenz.sechub.domain.statistic.job;

public enum JobStatisticDataType {

    UPLOAD(false, JobStatisticDataKey.SIZE_MB),

    ;

    private JobStatisticDataKey[] allowedKeys;

    JobStatisticDataType(boolean nullAccepted, JobStatisticDataKey... allowedKeys) {
        this.allowedKeys = allowedKeys;
    }

    public boolean isKeyAllowed(JobStatisticDataKey key) {
        for (JobStatisticDataKey allowedKey : allowedKeys) {
            if (key == allowedKey) {
                return true;
            }
        }
        return false;
    }
}
