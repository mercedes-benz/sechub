// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ScheduleLatestCipherPoolDataCalculatorTest {

    @Test
    void null_list_results_in_latest_pool_id_null() {

        /* prepare */
        ScheduleLatestCipherPoolDataCalculator calculatorToTest = new ScheduleLatestCipherPoolDataCalculator();

        List<ScheduleCipherPoolData> entries = null;

        /* execute */
        Long result = calculatorToTest.calculateLatestPoolId(entries);

        /* test */
        assertThat(result).isNull();
    }

    @Test
    void null_list_results_in_latest_pool_id_null_object_variant() {

        /* prepare */
        ScheduleLatestCipherPoolDataCalculator calculatorToTest = new ScheduleLatestCipherPoolDataCalculator();

        List<ScheduleCipherPoolData> entries = null;

        /* execute */
        ScheduleCipherPoolData result = calculatorToTest.calculateLatestPoolData(entries);

        /* test */
        assertThat(result).isNull();
    }

    @Test
    void empty_list_results_in_latest_pool_id_null() {

        /* prepare */
        ScheduleLatestCipherPoolDataCalculator calculatorToTest = new ScheduleLatestCipherPoolDataCalculator();

        List<ScheduleCipherPoolData> entries = new ArrayList<>();

        /* execute */
        Long result = calculatorToTest.calculateLatestPoolId(entries);

        /* test */
        assertThat(result).isNull();
    }

    @Test
    void empty_list_results_in_latest_pool_id_null_object_variant() {

        /* prepare */
        ScheduleLatestCipherPoolDataCalculator calculatorToTest = new ScheduleLatestCipherPoolDataCalculator();

        List<ScheduleCipherPoolData> entries = new ArrayList<>();

        /* execute */
        ScheduleCipherPoolData result = calculatorToTest.calculateLatestPoolData(entries);

        /* test */
        assertThat(result).isNull();
    }

    @Test
    void two_entries_newer_one_resolved() {

        /* prepare */
        ScheduleLatestCipherPoolDataCalculator calculatorToTest = new ScheduleLatestCipherPoolDataCalculator();

        // create valid cipher pool data
        ScheduleCipherPoolData data1 = new ScheduleCipherPoolData();
        data1.id = Long.valueOf(0);
        data1.created = LocalDateTime.now().minusDays(1);

        ScheduleCipherPoolData data2 = new ScheduleCipherPoolData();
        data2.id = Long.valueOf(1);
        data2.created = LocalDateTime.now();

        List<ScheduleCipherPoolData> entries = new ArrayList<>();
        entries.add(data1);
        entries.add(data2);

        /* execute */
        Long result = calculatorToTest.calculateLatestPoolId(entries);

        /* test */
        assertThat(result).describedAs("data2 is the newer one").isEqualTo(data2.id);
    }

    @Test
    void three_entries_newer_resolved_no_matter_that_inside_middle() {

        /* prepare */
        ScheduleLatestCipherPoolDataCalculator calculatorToTest = new ScheduleLatestCipherPoolDataCalculator();

        // create valid cipher pool data
        ScheduleCipherPoolData data1 = new ScheduleCipherPoolData();
        data1.id = Long.valueOf(0);
        data1.created = LocalDateTime.now().minusDays(2);

        ScheduleCipherPoolData data2 = new ScheduleCipherPoolData();
        data2.id = Long.valueOf(1);
        data2.created = LocalDateTime.now().minusDays(1);

        ScheduleCipherPoolData data3 = new ScheduleCipherPoolData();
        data3.id = Long.valueOf(2);
        data3.created = LocalDateTime.now();

        List<ScheduleCipherPoolData> entries = new ArrayList<>();
        entries.add(data1);
        entries.add(data3);
        entries.add(data2);

        /* execute */
        Long result = calculatorToTest.calculateLatestPoolId(entries);

        /* test */
        assertThat(result).describedAs("data3 is the newer one - even when added in middle of list").isEqualTo(data3.id);
    }

    @Test
    void three_entries_newer_resolved_no_matter_that_inside_middle_object_variant() {

        /* prepare */
        ScheduleLatestCipherPoolDataCalculator calculatorToTest = new ScheduleLatestCipherPoolDataCalculator();

        // create valid cipher pool data
        ScheduleCipherPoolData data1 = new ScheduleCipherPoolData();
        data1.id = Long.valueOf(0);
        data1.created = LocalDateTime.now().minusDays(2);

        ScheduleCipherPoolData data2 = new ScheduleCipherPoolData();
        data2.id = Long.valueOf(1);
        data2.created = LocalDateTime.now().minusDays(1);

        ScheduleCipherPoolData data3 = new ScheduleCipherPoolData();
        data3.id = Long.valueOf(2);
        data3.created = LocalDateTime.now();

        List<ScheduleCipherPoolData> entries = new ArrayList<>();
        entries.add(data1);
        entries.add(data3);
        entries.add(data2);

        /* execute */
        ScheduleCipherPoolData result = calculatorToTest.calculateLatestPoolData(entries);

        /* test */
        assertThat(result).describedAs("data3 is the newer one - even when added in middle of list").isEqualTo(data3);
    }

}
