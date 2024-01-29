// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

public class TestConstants {

    /**
     * System property for defining manual test /test applications. When system
     * property is <code>true</code>, the manual test will not be ignored/is no
     * longer disabled. The property is necessary to have those tests not executed
     * per default in gradle builds.
     */
    public static final String MANUAL_TEST_BY_DEVELOPER = "sechub.manual.test.by.developer";

    public static final String DESCRIPTION_DISABLED_BECAUSE_A_MANUAL_TEST = "This test shall not be used in normal test phases. It is a vehicle for developers to handle special edge cases. For example to test a special setup or to generate content "
            + "without starting the Spring Boot application or any Gradle task(so much faster)";

    public final static String SOURCECODE_ZIP = "sourcecode.zip";
    public final static String BINARIES_TAR = "binaries.tar";
}
