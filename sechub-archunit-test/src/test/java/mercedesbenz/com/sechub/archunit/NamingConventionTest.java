// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static mercedesbenz.com.sechub.archunit.ArchUnitImportOptions.*;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses
public class NamingConventionTest {

    @Test
    void classes_in_test_packages_containing_test_or_assert_in_name() {
        /* prepare */
        /* @formatter:off */
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOptions(ignoreFolders)
                .withImportOption(ignoreSechubOpenAPIJava)
                .withImportOption(ignoreSechubTestframework)
                .withImportOption(ignoreSharedkernelTest)
                .withImportOption(ignoreSechubApiJava)
                .withImportOption(ignoreJarFiles)
                .importPath(SECHUB_ROOT_PATH);

        /* execute + test */
        classes()
                .that()
                .resideInAPackage("..test..")
                .should()
                .haveSimpleNameContaining("Test")
                .orShould()
                .haveNameMatching(".*\\.Assert.*") // including inner classes
                .orShould()
                .haveNameMatching(".*Test\\$.*") // including inner classes
                .because("Tests classes should contain 'Test' or 'Assert' in their name.")
                .check(importedClasses);
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
                .because("Service classes should contain 'Service' or 'Executor' in their name.")
                .check(importedClasses);
        /* @formatter:on */
    }
}