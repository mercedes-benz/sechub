// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Contains different time units which are all countable /convertible into days.
 *
 * @author Albert Tregnaghi
 *
 */
public enum CountableInDaysTimeunit {
    @JsonAlias({ "day", "days", "DAY", "DAYS" })
    DAY(1),

    @JsonAlias({ "week", "weeks", "WEEK", "WEEKS" })
    WEEK(7),

    @JsonAlias({ "month", "months", "MONTH", "MONTHS" })
    MONTH(30),

    @JsonAlias({ "year", "years", "YEAR", "YEARS" })
    YEAR(365),

    ;

    private int multiplicatorDays;

    CountableInDaysTimeunit(int multiplicatorDays) {
        this.multiplicatorDays = multiplicatorDays;
    }

    public long getMultiplicatorDays() {
        return multiplicatorDays;
    }
}
