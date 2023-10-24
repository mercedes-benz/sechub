// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.adopt;

import org.junit.jupiter.api.Test;

class AdoptedSystemTestRuntimeVariableTest {

    @Test
    public void origin_and_adopted_are_content_equal() throws Exception {
        /* prepare */
        AdoptionChecker checker = new AdoptionChecker(AdoptedSystemTestRuntimeVariable.class);

        /* execute + test */
        checker.assertAdoptedClassEqualsFileLocatedAt("com.mercedesbenz.sechub.systemtest.config", "RuntimeVariable");
    }

}
