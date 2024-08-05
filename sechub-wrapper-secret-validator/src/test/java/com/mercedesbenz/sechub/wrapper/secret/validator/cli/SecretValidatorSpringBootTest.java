// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mercedesbenz.sechub.wrapper.secret.validator.execution.SecretValidatorExecutionService;
import com.mercedesbenz.sechub.wrapper.secret.validator.support.SarifImporterKeys;

import de.jcup.sarif_2_1_0.model.Location;
import de.jcup.sarif_2_1_0.model.Region;
import de.jcup.sarif_2_1_0.model.Result;
import de.jcup.sarif_2_1_0.model.Run;
import de.jcup.sarif_2_1_0.model.SarifSchema210;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties
@TestPropertySource(properties = { "secret.validator.config-file=src/test/resources/config-test-files/valid-files/test-config.json",
        "secret.validator.trust-all-certificates=false", "pds.job.result.file=src/test/resources/config-test-files/valid-files/test-result.txt" })
@ActiveProfiles("test")
class SecretValidatorSpringBootTest {

    @Autowired
    SecretValidatorExecutionService executionService;

    @Test
    void execution_service_with_correct_configuration_without_validation_categorizes_findings_with_default_configured() {
        /* execute */
        SarifSchema210 report = executionService.execute();

        /* test */
        Run run = report.getRuns().get(0);
        for (Result finding : run.getResults()) {
            // since all validation requests fail the default categorization of the config
            // file will be used which is high
            assertFindingHasSerecoSeverity("high", finding);
        }
    }

    private void assertFindingHasSerecoSeverity(String expectedSerecoSeverity, Result finding) {
        Location location = finding.getLocations().get(0);
        Region region = location.getPhysicalLocation().getRegion();
        Map<String, Object> additionalProperties = region.getProperties().getAdditionalProperties();
        String serecoSeverity = (String) additionalProperties.get(SarifImporterKeys.SECRETSCAN_SERECO_SEVERITY.getKey());

        assertEquals(expectedSerecoSeverity, serecoSeverity);

    }

}
