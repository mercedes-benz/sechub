// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.test.PojoTester.*;

import java.util.UUID;

import org.junit.Test;

public class SecHubJobTest {

    @Test
    public void test_equals_and_hashcode_correct_implemented() {
        /* prepare */
        ScheduleSecHubJob objectA = new ScheduleSecHubJob();
        objectA.uUID = UUID.randomUUID();

        ScheduleSecHubJob objectBequalToA = new ScheduleSecHubJob();
        objectBequalToA.uUID = objectA.uUID;

        ScheduleSecHubJob objectCnotEqualToAOrB = new ScheduleSecHubJob();
        objectCnotEqualToAOrB.uUID = UUID.randomUUID();

        /* test */
        testEqualsAndHashCodeCorrectImplemented(objectA, objectBequalToA, objectCnotEqualToAOrB);
    }

}
