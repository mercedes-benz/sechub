// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import static com.daimler.sechub.adapter.TimeConstants.*;

@Component
public class PDSInstallSetupImpl implements PDSInstallSetup {

    @Value("${sechub.adapter.pds.default.check.timetowait.milliseconds:" + 30 * TIME_1_SECOND_IN_MILLISECONDS + "}") 
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_CHECK_IN_MILLISECONDS)
    private int defaultTimeToWaitForNextCheckOperationInMilliseconds;

    @Value("${sechub.adapter.pds.default.timeout.minutes:" + 4 * 60 + "}") // 4 hours
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT_IN_MINUTES)
    private int defaultTimeOutInMinutes;

    public int getDefaultTimeToWaitForNextCheckOperationInMilliseconds() {
        return defaultTimeToWaitForNextCheckOperationInMilliseconds;
    }

    public int getDefaultTimeOutInMinutes() {
        return defaultTimeOutInMinutes;
    }

    @Override
    public boolean isAbleToScan(TargetType targetType) {
        if (targetType == null) {
            return false;
        }
        /*
         * Otherwise this setup will always answer true - will be done dynamically at
         * executor reading its configuration.
         */
        return true;
    }

}