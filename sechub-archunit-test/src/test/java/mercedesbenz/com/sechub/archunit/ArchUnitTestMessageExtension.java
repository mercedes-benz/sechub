package mercedesbenz.com.sechub.archunit;

import static mercedesbenz.com.sechub.archunit.ArchUnitRuntimeSupport.SECHUB_ARCHUNIT_IGNORE_FOLDERS;

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
            If you receive a duplicated error (e.g. Multiple entries with same key), you can use the following system property to ignore specific folders:
            %s
            Default is set to %s=bin,out
            Example to ignore Gradle build: %s=build
            Your system property: %s=%s
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
            String folderToIgnore = System.getProperty(SECHUB_ARCHUNIT_IGNORE_FOLDERS);
            System.out.printf((ARCHUNIT_SUPPORT_MESSAGE) + "%n", SECHUB_ARCHUNIT_IGNORE_FOLDERS, SECHUB_ARCHUNIT_IGNORE_FOLDERS, SECHUB_ARCHUNIT_IGNORE_FOLDERS,
                    SECHUB_ARCHUNIT_IGNORE_FOLDERS, folderToIgnore);
        }
    }
}
