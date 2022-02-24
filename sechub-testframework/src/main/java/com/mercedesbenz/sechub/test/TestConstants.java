package com.mercedesbenz.sechub.test;

public class TestConstants {

    /**
     * System property for defining manual test /test applications.
     */
    public static final String MANUAL_TEST_BY_DEVELOPER = "sechub.manual.test.by.developer";

    public static final String DESCRIPTION_DISABLED_BECAUSE_A_MANUAL_TEST_FOR_GENERATION = "This test can be used by developers to generate content. "
            + "Not necesary for normal CI tests. A developer can run the 'test' to produce very fast generated outtput, "
            + "without starting the spring boot application or any gradle task(so much faster)";
}
