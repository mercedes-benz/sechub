// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.tngtech.archunit.core.importer.ImportOption;

public class ArchUnitImportOptions {

    public static final Path SECHUB_ROOT_PATH = resolveRoothPath();

    private static final String SECHUB_PACKAGE_PREFIX = "com/mercedesbenz/sechub";

    private static Path resolveRoothPath() {
        try {
            return Paths.get("./../").toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /* Ignore specific directories */
    static ImportOption ignoreAllTests = location -> {
        return !location.contains("/test/"); // ignore any URI to sources that contains '/test/'
    };

    static ImportOption ignoreAllSrc = location -> {
        /* @formatter:off */
        return !location.contains("/main/") &&
                !location.contains("/production/") &&
                /* ignore plugin src files */
                !location.contains("/instrumented/") &&
                !location.contains("/instrumentCode/");
        /* @formatter:on */
    };

    static ImportOption ignoreNonSecHubPackages = location -> {
        boolean accepted = location.contains(SECHUB_PACKAGE_PREFIX); // ignore any URI to sources that does not remain in the sechub package'

        // Next follows a workaround for an existing build situation
        // where we had in build folders a pseudo src folder which contained
        // packages like "src.test.java.com.mercedesbenz.sechub.*"
        // It is unclear why exactly this happens (maybe because of an eclipse build)
        // but we exclude this here which is always correct:
        accepted = accepted && !location.contains("src/test/java/" + SECHUB_PACKAGE_PREFIX);
        accepted = accepted && !location.contains("src/main/java/" + SECHUB_PACKAGE_PREFIX);

        return accepted;
    };

    static ImportOption ignoreArchUnit = location -> {
        return !location.contains("/archunit/"); // ignore any URI to sources that contains '/archunit/'
    };

    static List<ImportOption> ignoreFolders = new ArchUnitRuntimeSupport().createImportOptionsIgnoreFolder(); // ignore specific folders e.g. build folders

    static ImportOption ignoreSechubOpenAPI = location -> {
        return !location.contains("/sechub-openapi/"); // ignore any URI to sources that contains '/sechub-openapi/'
    };

    static ImportOption ignoreSechubTestframework = location -> {
        return !location.contains("/sechub-testframework/"); // ignore any URI to sources that contains '/sechub-testframework/'
    };

    static ImportOption ignoreSharedkernelTest = location -> {
        return !location.contains("/sharedkernel/test/"); // ignore any URI to sources that contains '/sechub-shared-kernel/test/'
    };

    static ImportOption ignoreIntegrationTest = location -> {
        return !location.contains("/sechub-integrationtest/"); // ignore any URI to sources that contains '/sechub-integrationtest/'
    };

    static ImportOption ignoreDocGen = location -> {
        return !location.contains("/docgen/"); // ignore any URI to sources that contains '/docgen/'
    };

    static ImportOption ignoreDevelopertools = location -> {
        return !location.contains("/developertools/"); // ignore any URI to sources that contains '/developertools/'
    };

    static ImportOption ignoreTools = location -> {
        return !location.contains("/tools/"); // ignore any URI to sources that contains '/tools/'
    };

    static ImportOption ignoreBuildSrc = location -> {
        return !location.contains("/buildSrc/"); // ignore any URI to sources that contains '/buildSrc/'
    };

    static ImportOption ignoreExamples = location -> {
        return !location.contains("/sechub-examples/"); // ignore any URI to sources that contains '/sechub-examples/'
    };

    static ImportOption ignoreNessusAdapter = location -> {
        return !location.contains("/deprecated-sechub-adapter-nessus/"); // ignore any URI to sources that contains '/deprecated-sechub-adapter-nessus/'
    };

    static ImportOption ignoreNessusProduct = location -> {
        return !location.contains("/deprecated-sechub-scan-product-nessus/"); // ignore any URI to sources that contains
                                                                              // '/deprecated-sechub-scan-product-nessus/'
    };

    static ImportOption ignoreNetsparkerAdapter = location -> {
        return !location.contains("/deprecated-sechub-adapter-netsparker/"); // ignore any URI to sources that contains
                                                                             // '/deprecated-sechub-adapter-netsparker/'
    };

    static ImportOption ignoreNetsparkerProduct = location -> {
        return !location.contains("/deprecated-sechub-scan-product-netsparker/"); // ignore any URI to sources that contains
                                                                                  // '/deprecated-sechub-scan-product-netsparker/'
    };

    static ImportOption ignoreAnalyzerCLI = location -> {
        return !location.contains("/sechub-analyzer-cli/"); // ignore any URI to sources that contains '/sechub-analyzer-cli/'
    };

    static ImportOption ignoreSechubApiJava = location -> {
        return !location.contains("/sechub-api-java/"); // ignore any URI to sources that contains '/sechub-api-java/'
    };

    static ImportOption ignoreSechubTest = location -> {
        return !location.contains("/sechub-test/"); // ignore any URI to sources that contains '/sechub-test/'
    };

    static ImportOption ignoreSystemTest = location -> {
        return !location.contains("/sechub-systemtest/"); // ignore any URI to sources that contains '/sechub-systemtest/'
    };

    static ImportOption ignoreGenApi = location -> {
        return !location.contains("/api/internal/gen/"); // ignore any URI to sources that contains '/api/internal/gen/'
    };

    /* Ignore specific classes */
    static ImportOption ignoreProductIdentifierClass = location -> {
        return !location.contains("sharedkernel/ProductIdentifier"); // ignore any URI to sources that contains '/ProductIdentifier'
    };

    static ImportOption ignoreIntegrationTestClass = location -> {
        return !location.contains("IntegrationTest"); // ignore any URI to sources that contains '/IntegrationTest'
    };

    static ImportOption ignoreSchedulerSourcecodeUploadService = location -> {
        return !location.contains("SchedulerSourcecodeUploadService"); // ignore any URI to sources that contains '/SchedulerSourcecodeUploadService'
    };

    static ImportOption ignoreJarFiles = location -> {
        return !location.contains(".jar"); // ignore jar files'
    };
}
