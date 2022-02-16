// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AbstractInstallSetupTest {

    private TestAbstractInstallSetup setupToTest;

    @Before
    public void before() throws Exception {
        setupToTest = new TestAbstractInstallSetup();
    }

    @Test
    public void isabletoscan_returns_false_when_a_target_has_type_intranet_and_not_able_to_scanDaimmler_intranet() {
        /* prepare */

        /* execute + test */
        assertFalse(setupToTest.isAbleToScan(TargetType.INTRANET));
    }

    @Test
    public void isabletoscan_returns_true_when_a_target_has_type_intranet_and_is_able_to_scanDaimmler_intranet() {
        /* prepare */
        setupToTest.canScanDaimlerIntranet = true;

        /* execute + test */
        assertTrue(setupToTest.isAbleToScan(TargetType.INTRANET));
    }

    @Test
    public void isabletoscan_returns_false_when_a_target_has_type_internet_and_not_able_to_scanInternet() {
        /* execute + test */
        assertFalse(setupToTest.isAbleToScan(TargetType.INTERNET));
    }

    @Test
    public void isabletoscan_returns_truee_when_a_target_has_type_internet_and_is_able_to_scanInternet() {
        /* prepare */
        setupToTest.canScanInternet = true;

        /* execute + test */
        assertTrue(setupToTest.isAbleToScan(TargetType.INTERNET));
    }

}
