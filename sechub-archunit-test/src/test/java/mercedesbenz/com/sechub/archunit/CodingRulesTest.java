// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.accessTargetWhere;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.*;
import static mercedesbenz.com.sechub.archunit.ArchUnitImportOptions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses
@ExtendWith(ArchUnitTestMessageExtension.class)
public class CodingRulesTest {

    @Test
    void classes_should_not_throw_generic_exceptions() {
        /* prepare */
        JavaClasses importedClasses = ignoreTestGeneratedAndDeprecatedPackages();

        /* execute + test */
        NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(importedClasses);
    }

    @Test
    void classes_should_not_use_deprecated_members() {
        /* prepare */
        /* @formatter:off */
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ignoreAllTests)
                .withImportOptions(ignoreFolders)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreNessusAdapter)
                .withImportOption(ignoreNessusProduct)
                .withImportOption(ignoreNetsparkerAdapter)
                .withImportOption(ignoreNetsparkerProduct)
                .withImportOption(ignoreIntegrationTest)
                .withImportOption(ignoreSechubApiJava)
                .withImportOption(ignoreProductIdentifierClass)
                .withImportOption(ignoreIntegrationTestClass)
                .withImportOption(ignoreDevelopertools)
                .withImportOption(ignoreSchedulerSourcecodeUploadService)
                .withImportOption(ignoreSystemTest)
                .withImportOption(ignoreGenApi)
                .withImportOption(ignoreJarFiles)
                .withImportOption(ignoreNonSecHubPackages)
                .importPath(SECHUB_ROOT_PATH);

        /* execute + test */
        /* custom version of DEPRECATED_API_SHOULD_NOT_BE_USED */
        noClasses()
                .should(accessTargetWhere(JavaAccess.Predicates.target(annotatedWith(Deprecated.class)))
                        .as("access @Deprecated members"))
//                 Following lines were out-commented because of JsonSerialize annotation uses deprecated default implementation
//                .orShould(dependOnClassesThat(annotatedWith(Deprecated.class))
//                        .as("depend on @Deprecated classes"))
                .because("there should be a better alternative! ")
                .check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void assertion_error_must_have_detailed_message() {
        /* prepare */
        JavaClasses importedClasses = ignoreTestGeneratedAndDeprecatedPackages();

        /* execute + test */
        ASSERTIONS_SHOULD_HAVE_DETAIL_MESSAGE.check(importedClasses);
    }

    @Test
    void test_classes_should_be_in_the_same_package_as_implementation() {
        /* prepare */
        /* @formatter:off */
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOptions(ignoreFolders)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreSechubApiJava)
                .withImportOption(ignoreDocGen)
                .withImportOption(ignoreIntegrationTest)
                .withImportOption(ignoreSechubTest)
                .withImportOption(ignoreSystemTest)
                .withImportOption(ignoreGenApi)
                .withImportOption(ignoreJarFiles)
                .withImportOption(ignoreNonSecHubPackages)
                .importPath(SECHUB_ROOT_PATH);

        /* execute + test */
        testClassesShouldResideInTheSamePackageAsImplementation().check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void classes_should_not_use_java_util_logging() {
        /* prepare */
        JavaClasses importedClasses = ignoreTestGeneratedAndDeprecatedPackages();

        /* execute + test */
        NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(importedClasses);
    }

    @Test
    void classes_should_not_use_standard_streams() {
        /* prepare */
        /* @formatter:off */
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ignoreAllTests)
                .withImportOptions(ignoreFolders)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreIntegrationTest)
                .withImportOption(ignoreDocGen)
                .withImportOption(ignoreDevelopertools)
                .withImportOption(ignoreTools)
                .withImportOption(ignoreBuildSrc)
                .withImportOption(ignoreAnalyzerCLI)
                .withImportOption(ignoreExamples)
                .withImportOption(ignoreGenApi)
                .withImportOption(ignoreJarFiles)
                .withImportOption(ignoreNonSecHubPackages)
                .importPath(SECHUB_ROOT_PATH);

        /* execute + test */
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(importedClasses);
        /* @formatter:on */
    }

    private JavaClasses ignoreTestGeneratedAndDeprecatedPackages() {
        /* @formatter:off */
        return new ClassFileImporter()
                .withImportOption(ignoreAllTests)
                .withImportOptions(ignoreFolders)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreNessusAdapter)
                .withImportOption(ignoreNessusProduct)
                .withImportOption(ignoreNetsparkerAdapter)
                .withImportOption(ignoreNetsparkerProduct)
                .withImportOption(ignoreIntegrationTest)
                .withImportOption(ignoreSechubApiJava)
                .withImportOption(ignoreDevelopertools)
                .withImportOption(ignoreGenApi)
                .withImportOption(ignoreJarFiles)
                .withImportOption(ignoreNonSecHubPackages)
                .importPath(SECHUB_ROOT_PATH);
        /* @formatter:on */
    }
}
