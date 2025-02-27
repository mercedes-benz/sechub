// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification.user;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;

public class NewApiTokenRequestedUserNotificationServiceHelper {

    static final long DEFAULT_OUTDATED_TIME_MILLIS = 86400000;// 1d * 24h * 60m * 60s * 1000ms = one day = 86400000
    static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    @Value("${sechub.user.onetimetoken.outdated.millis:86400000}")
    @MustBeDocumented(value = "Time until the one time token expires", scope = DocumentationScopeConstants.SCOPE_API_TOKEN)
    long oneTimeOutDatedMillis = DEFAULT_OUTDATED_TIME_MILLIS;

    private final Clock clock;

    public NewApiTokenRequestedUserNotificationServiceHelper(Clock clock) {
        this.clock = clock;
    }

    protected String getApiTokenExpireDate() {
        DateTimeFormatter customFormat = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        LocalDateTime tokenExpireDate = calculateApiTokenExpireDate();
        return tokenExpireDate.format(customFormat);
    }

    protected LocalDateTime calculateApiTokenExpireDate() {
        LocalDateTime now = LocalDateTime.now(clock);
        return now.plus(oneTimeOutDatedMillis, ChronoUnit.MILLIS);
    }

}
