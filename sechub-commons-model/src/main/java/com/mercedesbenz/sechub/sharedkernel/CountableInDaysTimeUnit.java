// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Contains different time units which are all countable /convertible into days.
 *
 * @author Albert Tregnaghi
 *
 */
public enum CountableInDaysTimeUnit {
    @JsonAlias({ "day", "days", "DAYS" })
    DAY(1),

    @JsonAlias({ "week", "weeks", "WEEKS" })
    WEEK(7),

    @JsonAlias({ "month", "months", "MONTHS" })
    MONTH(30),

    @JsonAlias({ "year", "years", "YEARS" })
    YEAR(365),

    ;

    private int multiplicatorDays;

    CountableInDaysTimeUnit(int multiplicatorDays) {
        this.multiplicatorDays = multiplicatorDays;
    }

    public long getMultiplicatorDays() {
        return multiplicatorDays;
    }
}
