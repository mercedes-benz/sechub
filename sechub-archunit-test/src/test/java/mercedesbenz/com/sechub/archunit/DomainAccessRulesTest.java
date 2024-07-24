package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses
public class DomainAccessRulesTest {

    private JavaClasses importedClasses;

    private static final String COM_MERCEDESBENZ_SECHUB_DOMAIN_SCAN = "com.mercedesbenz.sechub.domain.scan";
    private static final String COM_MERCEDESBENZ_SECHUB_DOMAIN_ADMINISTRATION = "com.mercedesbenz.sechub.domain.administration";
    private static final String COM_MERCEDESBENZ_SECHUB_DOMAIN_STATISTIC = "com.mercedesbenz.sechub.domain.statistic";
    private static final String COM_MERCEDESBENZ_SECHUB_DOMAIN_SCHEDULE = "com.mercedesbenz.sechub.domain.schedule";
    private static final String COM_MERCEDESBENZ_SECHUB_DOMAIN_NOTIFICATION = "com.mercedesbenz.sechub.domain.notification";
    private static final String COM_MERCEDESBENZ_SECHUB_DOMAIN_AUTHORIZATION = "com.mercedesbenz.sechub.domain.authorization";

    /* @formatter:off */
    private static List<String> allDomainsToTest = Arrays.asList(
            COM_MERCEDESBENZ_SECHUB_DOMAIN_SCAN,
            COM_MERCEDESBENZ_SECHUB_DOMAIN_ADMINISTRATION,
            COM_MERCEDESBENZ_SECHUB_DOMAIN_STATISTIC,
            COM_MERCEDESBENZ_SECHUB_DOMAIN_SCHEDULE,
            COM_MERCEDESBENZ_SECHUB_DOMAIN_NOTIFICATION,
            COM_MERCEDESBENZ_SECHUB_DOMAIN_AUTHORIZATION
            );
    /* @formatter:on */

    @ParameterizedTest
    @ArgumentsSource(DomainDataArgumentProvider.class)
    void no_class_in_one_domain_communicate_with_another_domain(String domainToTest) {
        /* prepare */
        /* @formatter:off */
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPath("../../sechub/");

        /* execute + test */
        noClasses()
                .that()
                .resideInAPackage(packageIdentifier(domainToTest))
                .should()
                .accessClassesThat()
                .resideInAnyPackage(resolveOtherDomainsThan(domainToTest))
                .check(importedClasses);
        /* @formatter:on */
    }

    private static String[] resolveOtherDomainsThan(String domainToTest) {
        List<String> otherDomains = new ArrayList<>(allDomainsToTest);
        otherDomains.remove(domainToTest);
        String[] otherDomainsArray = new String[otherDomains.size()];
        for (int i = 0; i < otherDomains.size(); i++) {
            String packageIdentifier = packageIdentifier(otherDomains.get(i));
            otherDomainsArray[i] = packageIdentifier;
        }
        return otherDomainsArray;
    }

    private static String packageIdentifier(String domain) {
        return ".." + domain + "..";
    }

    private static class DomainDataArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return allDomainsToTest.stream().map(domain -> Arguments.of(domain));
        }
    }
}
