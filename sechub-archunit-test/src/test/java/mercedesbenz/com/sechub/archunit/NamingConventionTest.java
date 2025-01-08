// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static mercedesbenz.com.sechub.archunit.ArchUnitImportOptions.*;
import static mercedesbenz.com.sechub.archunit.ArchUnitRuntimeSupport.ARCHUNIT_SUPPORT_NOTE;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

@AnalyzeClasses
public class NamingConventionTest {

    @Test
    void classes_in_test_packages_containing_test_or_assert_in_name() {
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
                .haveSimpleNameContaining("Test")
                .orShould()
                .haveSimpleNameContaining("Assert")
                .orShould()
                .haveNameMatching(".*\\$.*") // ignoring inner classes
                .because("Tests classes should contain 'Test' or 'Assert' in their name. " + ARCHUNIT_SUPPORT_NOTE);

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
                .because("Service classes should contain 'Service' or 'Executor' in their name. " + ARCHUNIT_SUPPORT_NOTE)
                .check(importedClasses);
        /* @formatter:on */
    }
}