// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.adopt;

import org.junit.jupiter.api.Test;

class AdoptedSystemTestDefaultFallbackTest {

    @Test
    public void origin_and_adopted_are_content_equal() throws Exception {
        /* prepare */
        AdoptionChecker checker = new AdoptionChecker(AdoptedSystemTestDefaultFallback.class);

        /* execute + test */
        checker.assertAdoptedClassEqualsFileLocatedAt("com.mercedesbenz.sechub.systemtest.config", "DefaultFallback");
    }
}
