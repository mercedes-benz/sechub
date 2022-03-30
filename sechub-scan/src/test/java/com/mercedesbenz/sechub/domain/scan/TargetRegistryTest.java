// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static com.mercedesbenz.sechub.domain.scan.NetworkTargetType.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class TargetRegistryTest {

    private NetworkTargetRegistry registryToTest;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    @Before
    public void before() throws Exception {
        registryToTest = new NetworkTargetRegistry();
    }

    @Test
    public void nothing_registered_each_registry_info_contains_no_target() {
        for (NetworkTargetType type : NetworkTargetType.values()) {
            NetworkTargetInfo info = registryToTest.createRegistryInfo(type);
            assertFalse(info.containsAtLeastOneTarget());
        }
    }

    @Test
    public void registering_a_target_of_type_internet_returns_an_unmodifiable_list_of_targets() {

        /* prepare */
        NetworkTarget target1 = mock(NetworkTarget.class);
        when(target1.getType()).thenReturn(INTERNET);

        NetworkTarget target2 = mock(NetworkTarget.class);
        when(target1.getType()).thenReturn(INTERNET);

        /* execute */
        registryToTest.register(target1);

        /* test */
        List<NetworkTarget> targets = registryToTest.getTargetsFor(INTERNET);
        expected.expect(UnsupportedOperationException.class);// the next line will result in UOE, because unmodifiable
        targets.add(target2);

    }

    @Test
    public void registering_a_target_of_type_internet_returns_none_for_intranet_and_target_for_internet() throws Exception {

        /* prepare */
        NetworkTarget target = mock(NetworkTarget.class);
        when(target.getType()).thenReturn(INTERNET);

        /* execute */
        registryToTest.register(target);

        /* test */
        assertTrue(registryToTest.getTargetsFor(INTRANET).isEmpty());
        assertFalse(registryToTest.getTargetsFor(INTERNET).isEmpty());
        assertTrue(registryToTest.getTargetsFor(INTERNET).contains(target));

    }

    @Test
    public void registering_a_target_of_type_intranet_returns_none_for_internet_and_target_for_inranet() throws Exception {

        /* prepare */
        NetworkTarget target = mock(NetworkTarget.class);
        when(target.getType()).thenReturn(INTRANET);

        /* execute */
        registryToTest.register(target);

        /* test */
        assertTrue(registryToTest.getTargetsFor(INTERNET).isEmpty());
        assertFalse(registryToTest.getTargetsFor(INTRANET).isEmpty());
        assertTrue(registryToTest.getTargetsFor(INTRANET).contains(target));

    }

    @Test
    public void nothing_registered_get_targets_for_any_type_returns_not_null_but_empty_list() {
        for (NetworkTargetType type : NetworkTargetType.values()) {
            List<NetworkTarget> targetsFor = registryToTest.getTargetsFor(type);
            assertNotNull("Type " + type + " results in null ", targetsFor);
            assertTrue("Type " + type + " is not empty", targetsFor.isEmpty());
        }
    }

    @Test
    public void nothing_registered_get_targets_for_null_throws_illegal_argument_exception() {
        /* prepare test */
        expected.expect(IllegalArgumentException.class);

        /* execute */
        registryToTest.getTargetsFor(null);
    }

    @Test
    public void registering_null_target_throws_illegal_argument_exception() {
        /* prepare test */
        expected.expect(IllegalArgumentException.class);

        /* execute */
        registryToTest.register(null);
    }

}
