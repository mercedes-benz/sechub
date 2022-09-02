// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import static org.junit.Assert.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.mercedesbenz.sechub.domain.scan.NetworkTarget;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;

/**
 * Inside application-test.properties we have defined strategies, which will
 * treat "*.intranet.example.com/org" and "192.168.*.*" as INTRANET. <br>
 * <br>
 * This integration test checks if the configured values are really used
 *
 * @author Albert Tregnaghi
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class TargetResolverServiceSpringBootTest {

    @Autowired
    TargetResolverService serviceToTest;

    @Test
    public void product_failure_demo_example_org__is_INTERNET() {
        /* prepare */
        URI uri = URI.create("https://productfailure.demo.example.org");

        /* execute */
        NetworkTarget found = serviceToTest.resolveTarget(uri);

        /* test */
        assertEquals(new NetworkTarget(uri, NetworkTargetType.INTERNET), found);

    }

    @Test
    public void ip_172_217_22_99__IS_INTERNET() throws Exception {
        /* prepare */
        InetAddress address = Inet4Address.getByName("172.217.22.99");

        /* execute */
        NetworkTarget found = serviceToTest.resolveTarget(address);

        /* test */
        assertEquals(new NetworkTarget(address, NetworkTargetType.INTERNET), found);

    }

    @Test
    public void something_intranet_example_org__is_INTRANET() {
        /* prepare */
        URI uri = URI.create("https://something.intranet.example.org");

        /* execute */
        NetworkTarget found = serviceToTest.resolveTarget(uri);

        /* test */
        assertEquals(new NetworkTarget(uri, NetworkTargetType.INTRANET), found);

    }

    @Test
    public void ip_192_168_22_99__IS_INTRANET() throws Exception {
        /* prepare */
        InetAddress address = Inet4Address.getByName("192.168.22.99");

        /* execute */
        NetworkTarget found = serviceToTest.resolveTarget(address);

        /* test */
        assertEquals(new NetworkTarget(address, NetworkTargetType.INTRANET), found);

    }

}
