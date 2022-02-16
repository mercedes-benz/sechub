// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import static com.daimler.sechub.test.PojoTester.*;

import java.util.UUID;

import org.junit.Test;

public class ReportTest {

    @Test
    public void test_equals_and_hashcode_correct_implemented() {
        /* prepare */
        ScanReport objectA = new ScanReport();
        objectA.uUID = UUID.randomUUID();

        ScanReport objectBequalToA = new ScanReport();
        objectBequalToA.uUID = objectA.uUID;

        ScanReport objectCnotEqualToAOrB = new ScanReport();
        objectCnotEqualToAOrB.uUID = UUID.randomUUID();

        /* test */
        testEqualsAndHashCodeCorrectImplemented(objectA, objectBequalToA, objectCnotEqualToAOrB);
    }

}
