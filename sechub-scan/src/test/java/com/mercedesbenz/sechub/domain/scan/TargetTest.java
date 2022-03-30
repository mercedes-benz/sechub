// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TargetTest {

    @Test
    void target_with_address_and_type_internet_is_correct_handled() {
        /* execute */
        NetworkTarget target = new NetworkTarget(Mockito.mock(InetAddress.class), NetworkTargetType.INTERNET);

        /* test */
        assertTrue(target.getType().isInternet());
        assertTrue(target.getType().isValid());
        assertFalse(target.getType().isIntranet());
    }

    @Test
    void target_with_address_and_type_intranet_is_correct_handled() {
        /* execute */
        NetworkTarget target = new NetworkTarget(Mockito.mock(InetAddress.class), NetworkTargetType.INTRANET);

        /* test */
        assertTrue(target.getType().isIntranet());
        assertTrue(target.getType().isValid());
        assertFalse(target.getType().isInternet());
    }

}
