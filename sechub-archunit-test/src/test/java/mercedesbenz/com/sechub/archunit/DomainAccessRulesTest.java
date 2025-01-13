// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static mercedesbenz.com.sechub.archunit.ArchUnitImportOptions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses
@ExtendWith(ArchUnitTestMessageExtension.class)
public class DomainAccessRulesTest {

    private static final String DOMAIN_SCAN = "com.mercedesbenz.sechub.domain.scan";
    private static final String DOMAIN_ADMINISTRATION = "com.mercedesbenz.sechub.domain.administration";
    private static final String DOMAIN_STATISTIC = "com.mercedesbenz.sechub.domain.statistic";
    private static final String DOMAIN_SCHEDULE = "com.mercedesbenz.sechub.domain.schedule";
    private static final String DOMAIN_NOTIFICATION = "com.mercedesbenz.sechub.domain.notification";
    private static final String DOMAIN_AUTHORIZATION = "com.mercedesbenz.sechub.domain.authorization";

    /* @formatter:off */
    private static final List<String> allDomainsToTest = Arrays.asList(
            DOMAIN_SCAN,
            DOMAIN_ADMINISTRATION,
            DOMAIN_STATISTIC,
            DOMAIN_SCHEDULE,
            DOMAIN_NOTIFICATION,
            DOMAIN_AUTHORIZATION
            );
    /* @formatter:on */

    @ParameterizedTest
    @ArgumentsSource(DomainDataArgumentProvider.class)
    void no_class_in_one_domain_communicate_with_another_domain(String domainToTest) {
        /* prepare */
        /* @formatter:off */
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ignoreAllTests)
                .withImportOptions(ignoreFolders)
                .withImportOption(ignoreDevelopertools)
                .withImportOption(ignoreJarFiles)
                .importPath(SECHUB_ROOT_PATH);

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
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return allDomainsToTest.stream().map(Arguments::of);
        }
    }
}
