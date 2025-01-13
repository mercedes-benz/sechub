package mercedesbenz.com.sechub.archunit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ArchUnitTestMessageExtension implements AfterEachCallback, AfterAllCallback {

    private static boolean hasFailedTests = false;

    private final String ARCHUNIT_SUPPORT_MESSAGE = """
            ###########################
            One or more ArchUnit tests have failed!
            Please check if you violated the defined rules in your implementation.
            If not: clean and rebuild, as archunit works on the build.
            If you receive a duplicated error (e.g. Multiple entries with same key), you can use the following system property to ignore specific folders e.g.:
            -Dsechub.archunit.ignoreFolders=bin,out
            -- will ignore eclipse and intelliJ builds and only scan your gradle /build folder --
            ###########################
            """;

    @Override
    public void afterEach(ExtensionContext context) {
        if (context.getExecutionException().isPresent()) {
            hasFailedTests = true;
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (hasFailedTests) {
            System.out.println(ARCHUNIT_SUPPORT_MESSAGE);
        }
    }
}
