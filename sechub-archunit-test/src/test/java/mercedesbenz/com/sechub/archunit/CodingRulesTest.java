package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.library.GeneralCodingRules.*;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses
public class CodingRulesTest {

    private JavaClasses importedClasses;

    @Test
    void classes_should_not_throw_generic_exceptions() {
        /* prepare */
        importDefaultPackages();

        /* execute + test */
        NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(importedClasses);
    }

    @Test
    void classes_should_not_use_deprecated_members() {
        /* prepare */
        importDefaultPackages();

        /* execute + test */
        DEPRECATED_API_SHOULD_NOT_BE_USED.check(importedClasses);
    }

    @Test
    void assertion_error_must_have_detailed_message() {
        /* prepare */
        importDefaultPackages();

        /* execute + test */
        ASSERTIONS_SHOULD_HAVE_DETAIL_MESSAGE.check(importedClasses);
    }

    @Test
    void test_classes_should_be_in_the_same_package_as_implementation() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ignoreOpenAPI)
                .importPath("../../sechub/");

        /* execute + test */
        testClassesShouldResideInTheSamePackageAsImplementation().check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void classes_should_not_use_java_util_logging() {
        /* prepare */
        importDefaultPackages();

        /* execute + test */
        NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(importedClasses);
    }

    @Test
    void classes_should_not_use_standard_streams() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ignoreTests)
                .withImportOption(ignoreOpenAPI)
                .withImportOption(ignoreIntegrationTest)
                .withImportOption(ignoreDocGen)
                .withImportOption(ignoreDevelopertools)
                .withImportOption(ignoreTools)
                .withImportOption(ignoreBuildSrc)
                .withImportOption(ignoreAnalyzerCLI)
                .importPath("../../sechub/");

        /* execute + test */
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(importedClasses);
        /* @formatter:on */
    }

    ImportOption ignoreTests = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/test/"); // ignore any URI to sources that contains '/test/'
        }
    };

    ImportOption ignoreOpenAPI = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-openapi-java(*)/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreIntegrationTest = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-integrationtest/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreDocGen = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/docgen/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreDevelopertools = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/developertools/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreTools = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/tools/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreBuildSrc = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/buildSrc/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreExamples = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-examples/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreNessusAdapter = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/deprecated-sechub-adapter-nessus/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreNessusProduct = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/deprecated-sechub-scan-product-nessus/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreNetsparkerAdapter = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/deprecated-sechub-adapter-netsparker/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreNetsparkerProduct = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/deprecated-sechub-scan-product-netsparker/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    ImportOption ignoreAnalyzerCLI = new ImportOption() {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/sechub-analyzer-cli/"); // ignore any URI to sources that contains '/openapi/'
        }
    };

    private void importDefaultPackages() {
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ignoreTests)
                .withImportOption(ignoreOpenAPI)
                .withImportOption(ignoreNessusAdapter)
                .withImportOption(ignoreNessusProduct)
                .withImportOption(ignoreNetsparkerAdapter)
                .withImportOption(ignoreNetsparkerProduct)
                .importPath("../../sechub/");
        /* @formatter:on */
    }
}
