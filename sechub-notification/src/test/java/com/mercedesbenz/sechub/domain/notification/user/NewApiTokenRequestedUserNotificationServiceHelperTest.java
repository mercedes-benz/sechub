package com.mercedesbenz.sechub.domain.notification.user;

import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NewApiTokenRequestedUserNotificationServiceHelperTest {

    private NewApiTokenRequestedUserNotificationServiceHelper serviceToTest;

    private static String DATE_TO_TEST = "2023-01-24T10:15:30.00Z";

    @BeforeEach
    void beforeEach() {
        serviceToTest = new NewApiTokenRequestedUserNotificationServiceHelper(Clock.fixed(Instant.parse(DATE_TO_TEST), ZoneOffset.UTC));
        serviceToTest.oneTimeOutDatedMillis = 86400000;
    }

    @Test
    void calculateApiTokenExpireDate_return_plus_one_day_date() {
        /* execute */
        LocalDateTime tokenExpireDate = serviceToTest.calculateApiTokenExpireDate();

        /* test */
        assertEquals(2023, tokenExpireDate.getYear());
        assertEquals(1, tokenExpireDate.getMonthValue());
        assertEquals(25, tokenExpireDate.getDayOfMonth());
        assertEquals(10, tokenExpireDate.getHour());
        assertEquals(15, tokenExpireDate.getMinute());
    }
}
