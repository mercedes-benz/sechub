// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class SchedulerSecHubJobRuntimeDataTest {

    @Test
    void set_get_work_as_expected() {
        /* prepare */
        UUID executionUUID1 = UUID.randomUUID();
        UUID executionUUID2 = UUID.randomUUID();
        UUID sechubJobUUID1 = UUID.randomUUID();

        /* execute */
        SchedulerSecHubJobRuntimeData info = new SchedulerSecHubJobRuntimeData(sechubJobUUID1);

        /* test */
        expect(info, null, sechubJobUUID1);

        /* execute */
        info.setExecutionUUID(executionUUID1);

        /* test */
        expect(info, executionUUID1, sechubJobUUID1);

        /* execute */
        info.setExecutionUUID(executionUUID2);

        /* test */
        expect(info, executionUUID2, sechubJobUUID1);

        info.setExecutionUUID(null);

        /* test */
        expect(info, null, sechubJobUUID1);

    }

    private void expect(SchedulerSecHubJobRuntimeData uuids, UUID executionUUID, UUID sechubJobUUID) {
        assertEquals(executionUUID, uuids.getExecutionUUID());
        assertEquals(executionUUID == null ? null : executionUUID.toString(), uuids.getExecutionUUIDAsString());

        assertEquals(sechubJobUUID, uuids.getSecHubJobUUID());
        assertEquals(sechubJobUUID == null ? null : sechubJobUUID.toString(), uuids.getSecHubJobUUIDasString());

    }

}
