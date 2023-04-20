package com.mercedesbenz.sechub.api.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * This is not really a test but a generator. To have full access to the
 * classpath and to have not the generator inside the build artifacts, it was
 * simply defined as some "test". It is not executed per default, but only when
 * system property is set.
 *
 * @author Albert Tregnaghi
 *
 */
class ApiWrapperObjectsGeneratorManualTest {

    @Test
    @EnabledIfSystemProperty(named = "sechub.generate", matches = "true")
    void generate() throws Exception {

        boolean overwritePublicModelFiles = Boolean.getBoolean("sechub.generate.overwrite");

        new ApiWrapperObjectsGenerator().generateAndFormatWithSpotless(overwritePublicModelFiles);
    }

}
