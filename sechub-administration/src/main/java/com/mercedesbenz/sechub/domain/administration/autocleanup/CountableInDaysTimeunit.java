// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.autocleanup;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Contains different time units which are all countable /convertible into days.
 *
 * @author Albert Tregnaghi
 *
 */
public enum CountableInDaysTimeunit {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({ "day", "days", "DAY", "DAYS" })
    DAY(1),

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({ "week", "weeks", "WEEK", "WEEKS" })
    WEEK(7),

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({ "month", "months", "MONTH", "MONTHS" })
    MONTH(30),

    ;

    private int multiplicatorDays;

    CountableInDaysTimeunit(int multiplicatorDays) {
        this.multiplicatorDays = multiplicatorDays;
    }

    public long getMultiplicatorDays() {
        return multiplicatorDays;
    }
}
