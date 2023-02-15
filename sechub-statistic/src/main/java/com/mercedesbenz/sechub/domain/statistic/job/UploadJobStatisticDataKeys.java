// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

@MustBeKeptStable("Do NOT rename the enum entries - the name is used as statistic key value!")
public enum UploadJobStatisticDataKeys implements JobStatisticDataKey {

    SIZE_IN_BYTES;

    @Override
    public String getKeyValue() {
        return name();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getKeyValue();
    }
}
