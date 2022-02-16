// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.cluster;

import static org.junit.Assert.*;

import org.junit.Test;

public class PODTest {
    @Test
    public void null_is_handled() {
        /* execute */

        POD pod = new POD(null, null);
        /* test */
        assertEquals(null, pod.getName());
        assertEquals(null, pod.getNamespace());

        assertEquals(pod.toString(), "POD:name=null,namespace=null");
    }

    @Test
    public void meta_data_is_removed() {
        /* execute */

        POD pod = new POD("sechub-server-86f75fbd7d-5xdb5 (v1:metadata.name)", "default (v1:metadata.namespace)");
        /* test */
        assertEquals("sechub-server-86f75fbd7d-5xdb5", pod.getName());
        assertEquals("default", pod.getNamespace());
    }

}
