// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import com.tngtech.archunit.core.importer.ImportOption;

public class ArchUnitImportOptions {

    public static String SECHUB_PACKAGE = "com.mercedesbenz.sechub";

    /* Ignore specific directories */
    static ImportOption ignoreAllTests = location -> {
        return !location.contains("/test/"); // ignore any URI to sources that contains '/test/'
    };

    static ImportOption ignoreSechubOpenAPIJava = location -> {
        return !location.contains("/sechub-openapi-java/"); // ignore any URI to sources that contains '/sechub-openapi-java/'
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
}
