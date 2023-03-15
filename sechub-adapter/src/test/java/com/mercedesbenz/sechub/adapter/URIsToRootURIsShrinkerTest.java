// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.adapter.support.URIShrinkSupport;

public class URIsToRootURIsShrinkerTest {

    private URIShrinkSupport shrinkerToTest;
    private URI target1_a;
    private URI target1_b;
    private URI target1_c;
    private URI target2_a;
    private URI target2_b;
    private URI target1_expected_root;
    private URI target2_expected_root;

    @Before
    public void before() throws Exception {
        shrinkerToTest = new URIShrinkSupport();

        target1_a = new URI("https://www.mycoolstuff.com/app1");
        target1_b = new URI("https://www.mycoolstuff.com/app2");
        target1_c = new URI("https://www.mycoolstuff.com/app3");
        target1_expected_root = new URI("https://www.mycoolstuff.com");

        target2_a = new URI("https://www.othercoolstuff.com/app1");
        target2_b = new URI("https://www.othercoolstuff.com/app2");
        target2_expected_root = new URI("https://www.othercoolstuff.com");

        /* health check for test correct */
        assertEquals(target1_a.getHost(), target1_b.getHost());
        assertEquals(target1_b.getHost(), target1_c.getHost());

        assertEquals(target2_a.getHost(), target2_b.getHost());

        assertNotEquals(target1_a.getHost(), target2_a.getHost());

    }

    @Test
    public void called_with_null_returns_empty_list() throws Exception {

        /* execute */
        Set<URI> result = shrinkerToTest.shrinkToRootURIs(null);

        /* test */
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    @Test
    public void called_with_uri_containing_a_port_number_and_same_uri_with_no_portnumber_only_common_bas_uri_as_one_result() throws Exception {

        List<URI> list = new ArrayList<>();
        String commonBaseURI = "https://www.mycoolstuff.com";
        String commonURI = commonBaseURI + "/app1";
        list.add(new URI(commonURI + ":8080"));
        list.add(new URI(commonURI));

        /* execute */
        Set<URI> result = shrinkerToTest.shrinkToRootURIs(list);

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(URI.create(commonBaseURI), result.iterator().next());

    }

    @Test
    public void called_with_empty_list_returns_empty_list() throws Exception {

        /* execute */
        Set<URI> result = shrinkerToTest.shrinkToRootURIs(new ArrayList<>());

        /* test */
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    @Test
    public void when_target_urls_are_3_where_3_have_same_hostnames_are_shrinked_to_1() throws Exception {

        /* prepare */
        List<URI> list = new ArrayList<>();
        list.add(target1_a);
        list.add(target1_b);
        list.add(target1_c);

        /* execute */
        Set<URI> result = shrinkerToTest.shrinkToRootURIs(list);

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(target1_expected_root));

    }

    @Test
    public void when_target_urls_are_3_where_2_have_same_hostname_are_shrinked_to_2() throws Exception {

        /* prepare */
        List<URI> list = new ArrayList<>();
        list.add(target1_a);
        list.add(target1_b);
        list.add(target2_a);

        /* execute */
        Set<URI> result = shrinkerToTest.shrinkToRootURIs(list);

        /* test */
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(target1_expected_root));
        assertTrue(result.contains(target2_expected_root));

    }

}
