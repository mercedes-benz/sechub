// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;

public class JobParameterProviderTest {

    private static JobParameterProvider supportToTest;

    @BeforeAll
    static void beforeAll() {
        /* prepare */
        List<ProductExecutorConfigSetupJobParameter> jobParameters = new ArrayList<>();
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key1", "value1"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key2", "2"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key3", "true"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key4", "false"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key5", "TRUE"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter("key6", null));

        /* create */
        supportToTest = new JobParameterProvider(jobParameters);
    }

    @Test
    void support_returns_for_key_null_null_as_object() {
        assertThat(supportToTest.get(null)).isNull();
    }

    @Test
    void support_returns_for_key_null_fals_as_boolean() {
        assertThat(supportToTest.getBoolean(null)).isFalse();
    }

    @Test
    void support_returns_for_key_null_n1_as_integer() {
        assertThat(supportToTest.getInt(null)).isEqualTo(-1);
    }

    @Test
    void support_returns_for_key1_value1() {
        assertThat(supportToTest.get("key1")).isEqualTo("value1");
    }

    @Test
    void support_returns_for_key1_n1_when_fetched_as_int() {
        assertThat(supportToTest.getInt("key1")).isEqualTo(-1);
    }

    @Test
    void support_returns_for_key2_int_2() {
        assertThat(supportToTest.getInt("key2")).isEqualTo(2);
    }

    @Test
    void support_returns_for_key3_with_value_true_a_true() {
        assertThat(supportToTest.getBoolean("key3")).isEqualTo(true);
    }

    @Test
    void support_returns_for_key3_with_value_true_a_n1_when_fetched_as_integer() {
        assertThat(supportToTest.getInt("key3")).isEqualTo(-1);
    }

    @Test
    void support_returns_for_key5_with_value_TRUE_a_true() {
        assertThat(supportToTest.getBoolean("key5")).isEqualTo(true);
    }

    @Test
    void support_returns_for_key6_with_value_null_false() {
        assertThat(supportToTest.getBoolean("key6")).isEqualTo(false);
    }

    @Test
    void support_returns_for_key4_false() {
        assertThat(supportToTest.getBoolean("key4")).isEqualTo(false);
    }

    @Test
    void support_returns_for_an_unknown_key_false() {
        assertThat(supportToTest.getBoolean("i-am-unknown...")).isEqualTo(false);
    }

    @Test
    void support_returns_for_an_unknown_key_n1_when_fetched_as_integer() {
        assertThat(supportToTest.getInt("i-am-unknown...")).isEqualTo(-1);
    }

    @Test
    void support_returns_for_key1_false_when_forced_as_boolean() {
        assertThat(supportToTest.getBoolean("key1")).isEqualTo(false);
    }

}
