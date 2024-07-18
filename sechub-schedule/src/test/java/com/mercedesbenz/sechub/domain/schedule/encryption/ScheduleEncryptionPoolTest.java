package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;

class ScheduleEncryptionPoolTest {

    @Test
    void constructor_from_map_results_in_expected_getCipherForPoolId_results() {
        /* prepare */
        Map<Long, PersistentCipher> map = new HashMap<>();
        PersistentCipher cipher1 = mock(PersistentCipher.class);
        PersistentCipher cipher2 = mock(PersistentCipher.class);
        map.put(Long.valueOf(1), cipher1);
        map.put(Long.valueOf(2), cipher2);

        /* execute */
        ScheduleEncryptionPool poolToTest = new ScheduleEncryptionPool(map);

        /* test */
        assertThat(poolToTest.getCipherForPoolId(Long.valueOf(0))).isNull();
        assertThat(poolToTest.getCipherForPoolId(Long.valueOf(1))).isEqualTo(cipher1);
        assertThat(poolToTest.getCipherForPoolId(Long.valueOf(2))).isEqualTo(cipher2);
        assertThat(poolToTest.getCipherForPoolId(Long.valueOf(3))).isNull();
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, -1, 99 })
    void constructor_from_null_map_results_in_pool_but_getCipherForPoolId_returns_always_null(long value) {
        /* prepare */
        Map<Long, PersistentCipher> map = null;

        /* execute */
        ScheduleEncryptionPool poolToTest = new ScheduleEncryptionPool(map);

        /* test */
        assertThat(poolToTest.getCipherForPoolId(Long.valueOf(value))).isNull();
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, -1, 99 })
    void constructor_from_empty_map_results_in_pool_but_getCipherForPoolId_returns_always_null(long value) {
        /* prepare */
        Map<Long, PersistentCipher> map = new HashMap<>();

        /* execute */
        ScheduleEncryptionPool poolToTest = new ScheduleEncryptionPool(map);

        /* test */
        assertThat(poolToTest.getCipherForPoolId(Long.valueOf(value))).isNull();
    }

    @Test
    void getAllPoolIds_returns_keys_of_pool_map() {
        /* prepare */
        Map<Long, PersistentCipher> map = new HashMap<>();
        PersistentCipher cipher1 = mock(PersistentCipher.class);
        PersistentCipher cipher2 = mock(PersistentCipher.class);
        map.put(Long.valueOf(1), cipher1);
        map.put(Long.valueOf(2), cipher2);

        ScheduleEncryptionPool poolToTest = new ScheduleEncryptionPool(map);

        /* execute */
        assertThat(poolToTest.getAllPoolIds()).contains(1L, 2L).hasSize(2);
    }
}
