// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Component
public class PDSInstallSetupImpl implements PDSInstallSetup {

    @Value("${sechub.adapter.pds.default.scanresultcheck.period.minutes:1}") // check every minute
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_CHECK)
    private int defaultTimeToWaitForNextCheckOperationInMinutes;

    @Value("${sechub.adapter.pds.default.scanresultcheck.timeout.minutes:240}") // 4 hours
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
    private int defaultTimeOutInMinutes;

    public int getDefaultTimeToWaitForNextCheckOperationInMinutes() {
        return defaultTimeToWaitForNextCheckOperationInMinutes;
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