package com.mercedesbenz.sechub.domain.statistic.job;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.domain.statistic.AnyTextAsKey;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataKey;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataType;

@MustBeKeptStable("The enum names are used in DB for types. So do not rename or remove them.")
public enum JobStatisticDataType implements StatisticDataType {

    UPLOAD_SOURCES(UploadJobStatisticDataKeys.SIZE_IN_BYTES), UPLOAD_BINARIES(UploadJobStatisticDataKeys.SIZE_IN_BYTES),;

    private StatisticDataKey[] acceptedKeys;

    JobStatisticDataType() {
        throw new IllegalArgumentException("At least one accepted key must be defined!");
    }

    JobStatisticDataType(JobStatisticDataKey... acceptedKeys) {
        this.acceptedKeys = acceptedKeys;
    }

    public boolean isKeyAccepted(StatisticDataKey key) {
        if (key == null) {
            return false;
        }
        for (StatisticDataKey acceptedKey : acceptedKeys) {
            if (acceptedKey instanceof AnyTextAsKey) {
                if (key instanceof AnyTextAsKey) {
                    /* for this type any text is possible as key */
                    return true;
                }
            }
            if (key.equals(acceptedKey)) {
                return true;
            }
        }
        return false;
    }
}
