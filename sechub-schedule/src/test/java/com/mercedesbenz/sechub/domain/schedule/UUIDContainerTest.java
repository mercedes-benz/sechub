package com.mercedesbenz.sechub.domain.schedule;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class UUIDContainerTest {

    @Test
    void set_get_work_as_expected() {
        /* prepare */
        UUID executionUUID1 = UUID.randomUUID();
        UUID executionUUID2 = UUID.randomUUID();
        UUID sechubJobUUID1 = UUID.randomUUID();
        UUID sechubJobUUID2 = UUID.randomUUID();

        /* execute */
        UUIDContainer uuids = new UUIDContainer();

        /* test */
        expect(uuids, null, null);

        /* execute */
        uuids.setExecutionUUID(executionUUID1);

        /* test */
        expect(uuids, executionUUID1, null);

        /* execute */
        uuids.setExecutionUUID(executionUUID2);

        /* test */
        expect(uuids, executionUUID2, null);

        /* execute */
        uuids.setSecHubJobUUID(sechubJobUUID1);

        /* test */
        expect(uuids, executionUUID2, sechubJobUUID1);

        /* execute */
        uuids.setSecHubJobUUID(sechubJobUUID2);
        uuids.setExecutionUUID(null);

        /* test */
        expect(uuids, null, sechubJobUUID2);

    }

    private void expect(UUIDContainer uuids, UUID executionUUID, UUID sechubJobUUID) {
        assertEquals(executionUUID, uuids.getExecutionUUID());
        assertEquals(executionUUID == null ? null : executionUUID.toString(), uuids.getExecutionUUIDAsString());

        assertEquals(sechubJobUUID, uuids.getSecHubJobUUID());
        assertEquals(sechubJobUUID == null ? null : sechubJobUUID.toString(), uuids.getSecHubJobUUIDasString());

    }

}
