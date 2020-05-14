package com.daimler.sechub.sharedkernel.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HostnameBuilderTest {

    private HostnameBuilder builderToTest;

    @Before
    public void before() throws Exception {
        builderToTest = new HostnameBuilder();
    }

    @Test
    public void resolved_hostname_is_not_empty_or_null() {
        
        /* execute */
        String hostname = builderToTest.buildHostname();
        
        /* test */
        assertNotNull(hostname);
        assertFalse(hostname.isEmpty());
    }
    
    @Test
    public void resolved_hostname_is_not_localhost() {
        
        /* execute */
        String hostname = builderToTest.buildHostname();
        
        /* test */
        assertFalse(hostname.equals("localhost"));
        assertTrue(hostname.indexOf("localhost")==-1);
    }
    

}
