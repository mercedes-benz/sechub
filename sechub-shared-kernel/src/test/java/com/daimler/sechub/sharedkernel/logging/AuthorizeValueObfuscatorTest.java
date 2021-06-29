// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AuthorizeValueObfuscatorTest {

    private AuthorizeValueObfuscator obfuscatorToTest;

    @BeforeEach
    void beforeEach() {
        obfuscatorToTest = new AuthorizeValueObfuscator();
    }

    @Test
    void test_null_value() { 
        assertEquals("null", obfuscatorToTest.obfuscate(null, 100));
        assertEquals("null", obfuscatorToTest.obfuscate(null, 0));
        assertEquals("null", obfuscatorToTest.obfuscate(null, 10));
    }
    
    @Test
    void test_empty_value() { 
        assertEquals("***length:0", obfuscatorToTest.obfuscate("", 1));
        assertEquals("***length:0", obfuscatorToTest.obfuscate("", 0));
        assertEquals("***length:0", obfuscatorToTest.obfuscate("", -1));
    }
    
    @ParameterizedTest
    @CsvSource({
    /* @formatter:off */
          "Basic aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk,"
        + "Basic aW***length:58",
        
          "Basic aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHd,"
        + "***length:57",
        
          "aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk,"
        + "aW***length:52",
        
          "aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHd,"
        + "***length:51",
        /* @formatter:on */
    })
    void test_minsize_52(String origin, String expected) {
        assertEquals(expected, obfuscatorToTest.obfuscate(origin,52));
    }
    
    @ParameterizedTest
    @CsvSource({
    /* @formatter:off */
          "Basic aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk,"
        + "Basic aW***length:58",
        
          "Basic aW350LXalc3Rexample,"
        + "Basic aW***length:25",
      
          "Basic aW350LXab,"
        + "Basic aW***length:15",
               
          "Basic aW350LXa,"
        + "***length:14",
        
          "aW350LXalc3Rexample,"
        + "aW***length:19",
        
          "aW350LXab,"
        + "aW***length:9",
          
          "aW350LXa,"
        + "***length:8",
          
          "aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk,"
        + "aW***length:52",
    })
    void test_minsize_9(String origin, String expected) {
        assertEquals(expected, obfuscatorToTest.obfuscate(origin,9));
    }

}
