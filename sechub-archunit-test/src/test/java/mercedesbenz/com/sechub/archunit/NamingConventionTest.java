package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses
public class NamingConventionTest {

    private JavaClasses importedClasses;

    @Test
    void classes_in_test_packages_containing_test_or_assert_in_name() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ignoreOpenAPI)
                .importPath("../../sechub/");

        /* execute + test */
        classes()
                .that()
                .resideInAPackage("..test..")
                .should()
                .haveSimpleNameContaining("Test")
                .orShould()
                .haveSimpleNameContaining("Assert")
                .because("Tests classes should contain 'Test' or 'Assert' in their name.")
                .check(importedClasses);
        /* @formatter:on */
    }

    @Test
    void service_annotated_classes_contain_service_or_executor_in_name() {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ignoreTests)
                .withImportOption(ignoreOpenAPI)
                .importPath("../../sechub/");

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
}