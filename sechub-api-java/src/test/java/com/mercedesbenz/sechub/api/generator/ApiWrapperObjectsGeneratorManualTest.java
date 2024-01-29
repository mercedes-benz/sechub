// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.generator;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.ManualTest;
import com.mercedesbenz.sechub.test.TestConstants;

/**
 * This is not really a test but a generator. To have full access to the
 * classpath and to have not the generator inside the build artifacts, it was
 * simply defined as some "test". It is not executed per default, but only when
 * system property {@link TestConstants#MANUAL_TEST_BY_DEVELOPER} is set to
 * <code>true</code>.
 *
 * @author Albert Tregnaghi
 *
 */
class ApiWrapperObjectsGeneratorManualTest implements ManualTest {

    @Test
    void generate() throws Exception {

        boolean overwritePublicModelFiles = Boolean.getBoolean("sechub.generate.overwrite");

        new ApiWrapperObjectsGenerator().generateAndFormatWithSpotless(overwritePublicModelFiles);
    }

}
