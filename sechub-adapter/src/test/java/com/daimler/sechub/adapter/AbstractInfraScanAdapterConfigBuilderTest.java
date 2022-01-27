// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.daimler.sechub.adapter.support.URIShrinkSupport;
import com.daimler.sechub.adapter.testclasses.TestInfraScanAdapterConfigBuilder;
import com.daimler.sechub.adapter.testclasses.TestInfraScanAdapterConfigInterface;

public class AbstractInfraScanAdapterConfigBuilderTest {

    @Test
    public void rootURIShrinker_is_used_when_building() throws Exception {

        /* prepare */
        URIShrinkSupport shrinker = mock(URIShrinkSupport.class);
        TestInfraScanAdapterConfigBuilder builderToTest = new TestInfraScanAdapterConfigBuilder() {
            protected URIShrinkSupport createURIShrinker() {
                return shrinker;
            }
        };

        builderToTest.setProductBaseUrl("baseUrl");
        Set<URI> targetURIs = new LinkedHashSet<>();
        targetURIs.add(new URI("http://www.mycoolstuff.com/app1"));
        targetURIs.add(new URI("http://www.mycoolstuff.com/app2"));

        Set<URI> mockedShrink = new LinkedHashSet<>();
        mockedShrink.add(new URI("http://www.shrinked.com"));

        when(shrinker.shrinkToRootURIs(eq(targetURIs))).thenReturn(mockedShrink);

        /* execute */
        TestInfraScanAdapterConfigInterface config = builderToTest.setTargetURIs(targetURIs).build();

        /* test */
        verify(shrinker).shrinkToRootURIs(eq(targetURIs));
        assertEquals(mockedShrink, config.getRootTargetURIs());

    }

    @Test
    public void when_no_target_url_set_the_config_has_null_as_target_uri_and_string_and_an_empty_list_of_uris() throws Exception {

        /* execute */
        TestInfraScanAdapterConfigInterface configToTest = validConfigAnd().build();

        /* test */
        assertNull(configToTest.getTargetURI());
        assertNull(configToTest.getTargetAsString());

        assertNotNull(configToTest.getTargetURIs());
        assertTrue(configToTest.getTargetURIs().isEmpty());

    }

    @Test
    public void when_no_target_ips_set_the_config_has_an_empty_list_of_ips() throws Exception {

        /* execute */
        TestInfraScanAdapterConfigInterface configToTest = validConfigAnd().build();

        /* test */
        assertNotNull(configToTest.getTargetIPs());
        assertTrue(configToTest.getTargetIPs().isEmpty());

    }

    @Test
    public void when_one_target_ips_set_the_config_has_one_inet_adress_entry() throws Exception {

        /* execute */
        TestInfraScanAdapterConfigInterface configToTest = validConfigAnd().setTargetIP(InetAddress.getByName("192.168.1.1")).build();

        /* test */
        assertNotNull(configToTest.getTargetIPs());
        assertFalse(configToTest.getTargetIPs().isEmpty());
        assertTrue(configToTest.getTargetIPs().contains(InetAddress.getByName("192.168.1.1")));

    }

    @Test
    public void when_one_target_uri_is_set__target_uri_is_as_expected_and_list_is_1() throws Exception {

        /* prepare */
        String uriString = "http://www.my.cool.stuff.com";
        URI uri = new URI(uriString);
        /* execute */

        TestInfraScanAdapterConfigInterface configToTest = validConfigAnd().setTargetURIs(Collections.singleton(uri)).build();

        /* test */
        assertEquals(uri, configToTest.getTargetURI());
        assertEquals(uriString, configToTest.getTargetAsString());

        assertNotNull(configToTest.getTargetURIs());
        assertEquals(1, configToTest.getTargetURIs().size());

    }

    @Test
    public void when_target_uri_is_set_by_string__target_uri_is_as_expected_and_list_is_1() throws Exception {
        /* prepare */
        String uriString = "http://www.my.cool.stuff.com";
        URI uri = new URI(uriString);
        /* execute */

        TestInfraScanAdapterConfigInterface configToTest = validConfigAnd().setTargetURI(uri).build();

        /* test */
        assertEquals(uri, configToTest.getTargetURI());
        assertEquals(uriString, configToTest.getTargetAsString());

        assertNotNull(configToTest.getTargetURIs());
        assertEquals(1, configToTest.getTargetURIs().size());
    }

    @Test
    public void when_duplicated_target_uri_is_set__target_uri_is_as_expected_and_list_is_1() throws Exception {

        /* prepare */
        String uriString = "http://www.my.cool.stuff.com";
        URI uri = new URI(uriString);

        List<URI> uris = new ArrayList<>();
        uris.add(uri);
        uris.add(new URI(uriString)); // Duplicated entry

        /* execute */

        TestInfraScanAdapterConfigInterface configToTest = validConfigAnd().setTargetURIs(Collections.singleton(uri)).build();

        /* test */
        assertEquals(uri, configToTest.getTargetURI());
        assertEquals(uriString, configToTest.getTargetAsString());

        assertNotNull(configToTest.getTargetURIs());
        assertEquals(1, configToTest.getTargetURIs().size());

    }

    private TestInfraScanAdapterConfigBuilder validConfigAnd() {
        return new TestInfraScanAdapterConfigBuilder().setProductBaseUrl("baseUrl");
    }
}
