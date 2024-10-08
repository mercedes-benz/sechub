// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerSecHubJobRuntimeRegistryTest {

    private SchedulerSecHubJobRuntimeRegistry registryToTest;
    private UUID sechubJobUUID1;

    @BeforeEach
    void beforeEach() {
        registryToTest = new SchedulerSecHubJobRuntimeRegistry();

        sechubJobUUID1 = UUID.randomUUID();
    }

    @Test
    void register_null_data_is_just_ignored() {

        /* execute */
        registryToTest.register(null);

        /* test */
        assertThat(registryToTest.fetchAllSecHubJobUUIDs()).isEmpty();

    }

    @Test
    void register_data_with_sechub_job_uuid_null_throws_illegal_argument() {
        /* prepare */
        SchedulerSecHubJobRuntimeData data = mock(SchedulerSecHubJobRuntimeData.class);
        when(data.getExecutionUUID()).thenReturn(null);

        /* execute */
        assertThatThrownBy(() -> registryToTest.register(data)).isInstanceOf(IllegalArgumentException.class);

        /* test */
        assertThat(registryToTest.fetchAllSecHubJobUUIDs()).isEmpty();

    }

    @Test
    void one_registered_data_can_be_fetched() {
        /* prepare */
        SchedulerSecHubJobRuntimeData data = mock(SchedulerSecHubJobRuntimeData.class);
        when(data.getSecHubJobUUID()).thenReturn(sechubJobUUID1);

        /* execute */
        registryToTest.register(data);

        /* test */
        assertThat(registryToTest.fetchDataForSecHubJobUUID(sechubJobUUID1)).isSameAs(data);

    }

    @Test
    void unregistered_data_cannot_be_fetched_any_longer() {
        /* prepare */
        SchedulerSecHubJobRuntimeData data = mock(SchedulerSecHubJobRuntimeData.class);
        when(data.getSecHubJobUUID()).thenReturn(sechubJobUUID1);

        registryToTest.register(data);
        assertThat(registryToTest.fetchDataForSecHubJobUUID(sechubJobUUID1)).isSameAs(data);

        /* execute */
        registryToTest.unregisterBySecHubJobUUID(sechubJobUUID1);

        /* test */
        assertThat(registryToTest.fetchDataForSecHubJobUUID(sechubJobUUID1)).isNull();
        assertThat(registryToTest.fetchAllSecHubJobUUIDs()).isEmpty();

    }

    @Test
    void unregistering_with_null_sechub_job_uuid_throws_illegal_argument() {
        /* execute */
        assertThatThrownBy(() -> registryToTest.unregisterBySecHubJobUUID(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void multi_registered_one_unregistered_data_cannot_be_fetched_any_longer() {
        /* prepare */
        UUID sechubJobUUID2 = UUID.randomUUID();
        SchedulerSecHubJobRuntimeData data1 = mock(SchedulerSecHubJobRuntimeData.class);
        when(data1.getSecHubJobUUID()).thenReturn(sechubJobUUID1);

        SchedulerSecHubJobRuntimeData data2 = mock(SchedulerSecHubJobRuntimeData.class);
        when(data2.getSecHubJobUUID()).thenReturn(sechubJobUUID2);

        registryToTest.register(data1);
        registryToTest.register(data2);

        assertThat(registryToTest.fetchDataForSecHubJobUUID(sechubJobUUID1)).isSameAs(data1);
        assertThat(registryToTest.fetchDataForSecHubJobUUID(sechubJobUUID2)).isSameAs(data2);
        assertThat(registryToTest.fetchAllSecHubJobUUIDs()).hasSize(2);

        /* execute */
        registryToTest.unregisterBySecHubJobUUID(sechubJobUUID1);

        /* test */
        assertThat(registryToTest.fetchDataForSecHubJobUUID(sechubJobUUID1)).isNull();
        assertThat(registryToTest.fetchDataForSecHubJobUUID(sechubJobUUID2)).isSameAs(data2);
        assertThat(registryToTest.fetchAllSecHubJobUUIDs()).hasSize(1);

    }

    @Test
    void one_registered_data_has_uuids_in_fetchAllSecHubJobUUIDs() {
        /* prepare */
        SchedulerSecHubJobRuntimeData data = mock(SchedulerSecHubJobRuntimeData.class);
        when(data.getSecHubJobUUID()).thenReturn(sechubJobUUID1);

        /* execute */
        registryToTest.register(data);

        /* test */
        assertThat(registryToTest.fetchAllSecHubJobUUIDs()).contains(sechubJobUUID1).hasSize(1);

    }

    @Test
    void multiple_registered_data_has_uuids_in_fetchAllSecHubJobUUIDs() {
        /* prepare */
        SchedulerSecHubJobRuntimeData data1 = new SchedulerSecHubJobRuntimeData(sechubJobUUID1);
        UUID sechubJobUUID2 = UUID.randomUUID();
        SchedulerSecHubJobRuntimeData data2 = new SchedulerSecHubJobRuntimeData(sechubJobUUID2);

        /* execute */
        registryToTest.register(data1);
        registryToTest.register(data2);

        /* test */
        assertThat(registryToTest.fetchAllSecHubJobUUIDs()).contains(sechubJobUUID1).contains(sechubJobUUID2).hasSize(2);

    }

    @Test
    void multiple_registered_data_can_be_fetched() {
        /* prepare */
        SchedulerSecHubJobRuntimeData data1 = new SchedulerSecHubJobRuntimeData(sechubJobUUID1);
        UUID sechubJobUUID2 = UUID.randomUUID();
        SchedulerSecHubJobRuntimeData data2 = new SchedulerSecHubJobRuntimeData(sechubJobUUID2);

        /* execute */
        registryToTest.register(data1);
        registryToTest.register(data2);

        /* test */
        assertThat(registryToTest.fetchDataForSecHubJobUUID(sechubJobUUID1)).isSameAs(data1);
        assertThat(registryToTest.fetchDataForSecHubJobUUID(sechubJobUUID2)).isSameAs(data2);

    }

}
