package mercedesbenz.com.sechub.archunit;

import static mercedesbenz.com.sechub.archunit.ArchUnitRuntimeSupport.SECHUB_ARCHUNIT_IGNORE_FOLDERS;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchUnitTestMessageExtension implements AfterEachCallback, AfterAllCallback {

    private List<String> failedTests = new ArrayList<>();

    private static final Logger LOG = LoggerFactory.getLogger(ArchUnitTestMessageExtension.class);

    static {
        /*
         * we always inform per INFO log entry, what folders are ignored at arch unit
         * execution
         */
        String folderToIgnore = System.getProperty(SECHUB_ARCHUNIT_IGNORE_FOLDERS);
        LOG.info("Arch unit tests running with {}={}", SECHUB_ARCHUNIT_IGNORE_FOLDERS, folderToIgnore);
    }

    private final String ARCHUNIT_SUPPORT_MESSAGE = """
            ###################################################################################################
            ArchUnit Test '%s'
            has failing tests:
            %s
            ###################################################################################################

            Please check if you violated the defined rules in your implementation.
            If not: clean and rebuild, as archunit works on the build.
            For duplicated class output files you can use the system property
            '-D%s=${unwantedFolders}' to ignore specific folders.
            ${unwantedFolders} can be a comma separated list or a single entry.

            If the system property is blank or not defined, the default value is '%s'
            which ignores Eclipse and IntelliJ native build outputs. This setup
            is always used in gradle builds so it is possible to use
            IDE native builds and gradle builds together without conflicts.

            If you want to run arch unit tests on your native IDE build outputs, you have
            to exclude the gradle build output via: -D%s=build

                    Remark: your current setup is:  -D%s=%s

            """;

    @Override
    public void afterEach(ExtensionContext context) {
        if (context.getExecutionException().isPresent()) {
            Optional<Method> method = context.getTestMethod();
            if (method.isPresent()) {
                failedTests.add(method.get().getName());
            } else {
                failedTests.add("Unknown method");
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {

        if (!failedTests.isEmpty()) {

            StringBuilder failedTestSb = new StringBuilder();
            for (String failedTest : failedTests) {
                failedTestSb.append("- '");
                failedTestSb.append(failedTest);
                failedTestSb.append("'\n");
            }

            String testClassName = context.getTestClass().orElse(Class.class).getName();

            System.out.printf(ARCHUNIT_SUPPORT_MESSAGE,

                    testClassName,

                    failedTestSb.toString(),

                    SECHUB_ARCHUNIT_IGNORE_FOLDERS,

                    ArchUnitRuntimeSupport.DEFAULT_IGNORED_FOLDERS,

                    SECHUB_ARCHUNIT_IGNORE_FOLDERS,

                    SECHUB_ARCHUNIT_IGNORE_FOLDERS,

                    ArchUnitRuntimeSupport.getFoldersToIgnore());
        }
    }
}
