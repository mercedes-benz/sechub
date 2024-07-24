package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.library.GeneralCodingRules.*;
import static mercedesbenz.com.sechub.archunit.ArchUnitImportOptions.*;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses
public class CodingRulesTest {

    private JavaClasses importedClasses;

    @Test
    void classes_should_not_throw_generic_exceptions() {
        /* prepare */
        ignoreTestGeneratedAndDeprecatedPackages();

        /* execute + test */
        NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(importedClasses);
    }

    @Test
    void classes_should_not_use_deprecated_members() {
        /* prepare */
        /* @formatter:off */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ignoreAllTests)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreNessusAdapter)
                .withImportOption(ignoreNessusProduct)
                .withImportOption(ignoreNetsparkerAdapter)
                .withImportOption(ignoreNetsparkerProduct)
                .withImportOption(ignoreIntegrationTest)
                .withImportOption(ignoreSechubApiJava)
                .withImportOption(ignoreProductIdentifierClass)
                .importPath("../../sechub/");
        /* @formatter:on */

        /* execute + test */
        DEPRECATED_API_SHOULD_NOT_BE_USED.check(importedClasses);
    }

    @Test
    void assertion_error_must_have_detailed_message() {
        /* prepare */
        ignoreTestGeneratedAndDeprecatedPackages();

        /* execute + test */
        ASSERTIONS_SHOULD_HAVE_DETAIL_MESSAGE.check(importedClasses);
    }

    @Test
    void test_classes_should_be_in_the_same_package_as_implementation() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreSechubApiJava)
                .withImportOption(ignoreDocGen)
                .withImportOption(ignoreIntegrationTest)
                .withImportOption(ignoreSechubTest)
                .importPath("../../sechub/");

        /* execute + test */
        testClassesShouldResideInTheSamePackageAsImplementation().check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void classes_should_not_use_java_util_logging() {
        /* prepare */
        ignoreTestGeneratedAndDeprecatedPackages();

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
                .withImportOption(ignoreAllTests)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreIntegrationTest)
                .withImportOption(ignoreDocGen)
                .withImportOption(ignoreDevelopertools)
                .withImportOption(ignoreTools)
                .withImportOption(ignoreBuildSrc)
                .withImportOption(ignoreAnalyzerCLI)
                .withImportOption(ignoreExamples)
                .importPath("../../sechub/");

        /* execute + test */
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(importedClasses);
        /* @formatter:on */
    }

    private void ignoreTestGeneratedAndDeprecatedPackages() {
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ignoreAllTests)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreNessusAdapter)
                .withImportOption(ignoreNessusProduct)
                .withImportOption(ignoreNetsparkerAdapter)
                .withImportOption(ignoreNetsparkerProduct)
                .withImportOption(ignoreIntegrationTest)
                .withImportOption(ignoreSechubApiJava)
                .importPath("../../sechub/");
        /* @formatter:on */
    }
}
