package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses
public class DomainAccessRulesTest {

    private JavaClasses importedClasses;

    @Test
    void no_java_class_in_package_com_mercedesbenz_sechub_domain_scan_imports_from_another_domain() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPath("../../sechub/");

        /* execute + test */
        noClasses()
                .that()
                .resideInAPackage("..com.mercedesbenz.sechub.domain.scan..")
                .should()
                .accessClassesThat()
                .resideInAnyPackage("..com.mercedesbenz.sechub.domain.administration..",
                        "..com.mercedesbenz.sechub.domain.statistic..",
                        "..com.mercedesbenz.sechub.domain.schedule..",
                        "..com.mercedesbenz.sechub.domain.notification..",
                        "..com.mercedesbenz.sechub.domain.authorization..")
                .check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void no_java_class_in_package_com_mercedesbenz_sechub_domain_administration_imports_from_another_domain() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPath("../../sechub/");

        /* execute + test */
        noClasses()
                .that()
                .resideInAPackage("..com.mercedesbenz.sechub.domain.administration..")
                .should()
                .accessClassesThat()
                .resideInAnyPackage("..com.mercedesbenz.sechub.domain.scan..",
                        "..com.mercedesbenz.sechub.domain.statistic..",
                        "..com.mercedesbenz.sechub.domain.schedule..",
                        "..com.mercedesbenz.sechub.domain.notification..",
                        "..com.mercedesbenz.sechub.domain.authorization..")
                .check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void no_java_class_in_package_com_mercedesbenz_sechub_domain_statistic_imports_from_another_domain() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPath("../../sechub/");

        /* execute + test */
        noClasses()
                .that()
                .resideInAPackage("..com.mercedesbenz.sechub.domain.statistic..")
                .should()
                .accessClassesThat()
                .resideInAnyPackage("..com.mercedesbenz.sechub.domain.scan..",
                        "..com.mercedesbenz.sechub.domain.administration..",
                        "..com.mercedesbenz.sechub.domain.schedule..",
                        "..com.mercedesbenz.sechub.domain.notification..",
                        "..com.mercedesbenz.sechub.domain.authorization..")
                .check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void no_java_class_in_package_com_mercedesbenz_sechub_domain_schedule_imports_from_another_domain() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPath("../../sechub/");

        /* execute + test */
        noClasses()
                .that()
                .resideInAPackage("..com.mercedesbenz.sechub.domain.schedule..")
                .should()
                .accessClassesThat()
                .resideInAnyPackage("..com.mercedesbenz.sechub.domain.scan..",
                        "..com.mercedesbenz.sechub.domain.administration..",
                        "..com.mercedesbenz.sechub.domain.statistic..",
                        "..com.mercedesbenz.sechub.domain.notification..",
                        "..com.mercedesbenz.sechub.domain.authorization..")
                .check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void no_java_class_in_package_com_mercedesbenz_sechub_domain_notification_imports_from_another_domain() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPath("../../sechub/");

        /* execute + test */
        noClasses()
                .that()
                .resideInAPackage("..com.mercedesbenz.sechub.domain.notification..")
                .should()
                .accessClassesThat()
                .resideInAnyPackage("..com.mercedesbenz.sechub.domain.scan..",
                        "..com.mercedesbenz.sechub.domain.administration..",
                        "..com.mercedesbenz.sechub.domain.statistic..",
                        "..com.mercedesbenz.sechub.domain.schedule..",
                        "..com.mercedesbenz.sechub.domain.authorization..")
                .check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void no_java_class_in_package_com_mercedesbenz_sechub_domain_authorization_imports_from_another_domain() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPath("../../sechub/");

        /* execute + test */
        noClasses()
                .that()
                .resideInAPackage("..com.mercedesbenz.sechub.domain.authorization..")
                .should()
                .accessClassesThat()
                .resideInAnyPackage("..com.mercedesbenz.sechub.domain.scan..",
                        "..com.mercedesbenz.sechub.domain.administration..",
                        "..com.mercedesbenz.sechub.domain.statistic..",
                        "..com.mercedesbenz.sechub.domain.schedule..",
                        "..com.mercedesbenz.sechub.domain.notification..")
                .check(importedClasses);
        /* @formatter:on */
    }
}
