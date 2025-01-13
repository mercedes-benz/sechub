// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static mercedesbenz.com.sechub.archunit.ArchUnitImportOptions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.stereotype.Service;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

@AnalyzeClasses
@ExtendWith(ArchUnitTestMessageExtension.class)
public class NamingConventionTest {

    @Test
    void classes_in_test_packages_start_with_test_or_assert_or_end_with_test() {
        /* prepare */
        /* @formatter:off */
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOptions(ignoreFolders)
                .withImportOption(ignoreAllMain)
                .withImportOption(ignoreArchUnit)
                .withImportOption(ignoreDocGen)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreSechubTestframework)
                .withImportOption(ignoreSharedkernelTest)
                .withImportOption(ignoreSechubApiJava)
                .withImportOption(ignoreJarFiles)
                .importPath(SECHUB_ROOT_PATH);

        /* execute + test */
        // workaround for resideInAnyPackage not working, using ignoreFolders (ignore main) instead
        ArchRule rule = ArchRuleDefinition.classes()
                .should()
                .haveSimpleNameEndingWith("Test")
                .orShould()
                .haveSimpleNameStartingWith("Test")
                .orShould()
                .haveSimpleNameStartingWith("Assert")
                .orShould()
                .haveNameMatching(".*\\$.*") // ignoring inner classes
                .because("Tests classes should start or end with 'Test' or start with 'Assert'");

        rule.check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void service_annotated_classes_contain_service_or_executor_in_name() {
        /* prepare */
        /* @formatter:off */
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOptions(ignoreFolders)
                .withImportOption(ignoreAllTests)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreJarFiles)
                .importPath(SECHUB_ROOT_PATH);

        /* execute + test */
        classes()
                .that()
                .areAnnotatedWith(Service.class)
                .should()
                .haveSimpleNameContaining("Service")
                .orShould()
                .haveSimpleNameContaining("Executor")
                .because("Service classes should contain 'Service' or 'Executor' in their name. ")
                .check(importedClasses);
        /* @formatter:on */
    }
}