// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static com.mercedesbenz.sechub.adapter.TimeConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;

@Component
public class PDSInstallSetupImpl implements PDSInstallSetup {

    @Value("${sechub.adapter.pds.default.check.timetowait.milliseconds:" + 30 * TIME_1_SECOND_IN_MILLISECONDS + "}")
    @MustBeDocumented(value = AbstractAdapterConfigBuilder.DOCUMENT_INFO_CHECK_IN_MILLISECONDS, scope = DocumentationScopeConstants.SCOPE_PDS)
    private int defaultTimeToWaitForNextCheckOperationInMilliseconds;

    @Value("${sechub.adapter.pds.default.timeout.minutes:" + 4 * 60 + "}") // 4 hours
    @MustBeDocumented(value = AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT_IN_MINUTES, scope = DocumentationScopeConstants.SCOPE_PDS)
    private int defaultTimeOutInMinutes;

    public int getDefaultTimeToWaitForNextCheckOperationInMilliseconds() {
        return defaultTimeToWaitForNextCheckOperationInMilliseconds;
    }

    public int getDefaultTimeOutInMinutes() {
        return defaultTimeOutInMinutes;
    }

}