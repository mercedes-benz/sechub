// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class AuthorizeValueObfuscatorTest {

    private AuthorizeValueObfuscator obfuscatorToTest;

    @BeforeEach
    void beforeEach() {
        obfuscatorToTest = new AuthorizeValueObfuscator();
    }

    @ParameterizedTest
    @CsvSource({
    /* @formatter:off */
          "Basic aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk,"
        + "Basic aW35************************************************",
        
          "Basic aW350LXalc3Rexample,"
        + "Basic aW35***************",
      
          "Basic aW350LXab,"
        + "Basic aW35*****",
               
          "Basic aW350LXa,"
        + "*****length was:14",
        
          "aW350LXalc3Rexample,"
        + "aW35***************",
        
          "aW350LXab,"
        + "aW35*****",
          
          "aW350LXa,"
        + "*****length was:8",
          
          "aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk,"
        + "aW35************************************************",
        
          "banana,"
        + "*****length was:6",
          "lemon,"
        + "*****length was:5"
        /* @formatter:on */
    })
    void test(String origin, String expected) {
        assertEquals(expected, obfuscatorToTest.obfuscate(origin));
    }

}
