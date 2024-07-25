package mercedesbenz.com.sechub.archunit;

import java.util.List;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

public class ArchUnitImportOptions {

    public static final String SECHUB_ROOT_FOLDER = "../../sechub/";

    static List<ImportOption> ignoreBuildFolders = new ArchUnitRuntimeSupport().createImportOptionsForBuildSystem();

    /* Ignore specific packages */
    static ImportOption ignoreAllTests = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/test/"); // ignore any URI to sources that contains '/test/'
        }
    };

    static ImportOption ignoreSechubOpenAPIJava = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-openapi-java(*)/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreSechubTestframework = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-testframework/"); // ignore any URI to sources that contains '/sechub-testframework/'
        }
    };

    static ImportOption ignoreSharedkernelTest = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("com/mercedesbenz/sechub/sharedkernel/test"); // ignore any URI to sources that contains '/sechub-shared-kernel/'
        }
    };

    static ImportOption ignoreIntegrationTest = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-integrationtest/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreDocGen = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/docgen/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreDevelopertools = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/developertools/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreTools = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/tools/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreBuildSrc = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/buildSrc/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreExamples = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-examples/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreNessusAdapter = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/deprecated-sechub-adapter-nessus/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreNessusProduct = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/deprecated-sechub-scan-product-nessus/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreNetsparkerAdapter = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/deprecated-sechub-adapter-netsparker/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreNetsparkerProduct = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/deprecated-sechub-scan-product-netsparker/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreAnalyzerCLI = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-analyzer-cli/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreSechubApiJava = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-api-java/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    static ImportOption ignoreSechubTest = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-test/"); // ignore any URI to sources that contains '/sechub-test/'
        }
    };

    static ImportOption ignoreSystemTest = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-systemtest/"); // ignore any URI to sources that contains '/sechub-systemtest/'
        }
    };

    static ImportOption ignoreGenApi = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/api/internal/gen/"); // ignore any URI to sources that contains '/sechub-gen-api/'
        }
    };

    /* Ignore specific classes */
    static ImportOption ignoreProductIdentifierClass = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("sharedkernel/ProductIdentifier"); // ignore any URI to sources that contains '/ProductIdentifier'
        }
    };

    static ImportOption ignoreIntegrationTestClass = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("IntegrationTest"); // ignore any URI to sources that contains '/ProductResult'
        }
    };

    static ImportOption ignoreSchedulerSourcecodeUploadService = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("SchedulerSourcecodeUploadService"); // ignore any URI to sources that contains '/ProductResult'
        }
    };
}
