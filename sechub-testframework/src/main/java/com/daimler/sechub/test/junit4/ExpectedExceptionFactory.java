// SPDX-License-Identifier: MIT
package com.daimler.sechub.test.junit4;

import org.junit.rules.ExpectedException;

/**
 * Purpose of this class: Origin junit 4 ExpectedException.none() is set as
 * deprecated. The proposal for new usage is ... not the way we want (e.g. with
 * old style its possible to to identifiy exception having only a specific part
 * inside message etc.). To prevent rewrite of all tests we have created this helper
 * class. if the origin ExpectedException is moved away from junit4 we can
 * either reimplement ExpectedException, or we have already moved to junit5 where a complete, big
 * refactoring of tests will be necessary (e.g. because method signatures for
 * asserts etc. are totally different) so rules will become obsolete.
 * 
 * @author Albert Tregnaghi
 *
 */
public class ExpectedExceptionFactory {

    /**
     * Return rule to handle expectations about exceptions.
     * 
     * @return a rule to check there is no exception thrown - change this rule object with your needs
     */
    @SuppressWarnings("deprecation")
    public static ExpectedException none() {
        /*
         * at the moment we use old junit rule - which is deprecated, but ... is simpler
         * to use than the wanted new way etc.
         */
        return ExpectedException.none();
    }
}
