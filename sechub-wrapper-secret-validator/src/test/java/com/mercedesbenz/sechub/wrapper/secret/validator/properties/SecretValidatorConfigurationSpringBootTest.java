// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = { SecretValidatorProperties.class, SecretValidatorPDSJobResult.class })
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties
@TestPropertySource(properties = { "secret.validator.config-file=src/test/resources/config-test-files/valid-files/test-config.json",
        "secret.validator.trust-all-certificates=false", "pds.job.result.file=src/test/resources/config-test-files/valid-files/test-result.txt" })
class SecretValidatorConfigurationSpringBootTest {

    @Autowired
    private SecretValidatorProperties properties;

    @Autowired
    private SecretValidatorPDSJobResult pdsJobResult;

    @Test
    void properties_are_created_correctly() {
        /* test */

        // check if all SecretValidatorProperties are as expected
        assertFalse(properties.isTrustAllCertificates());
        assertEquals("src/test/resources/config-test-files/valid-files/test-config.json", properties.getConfigFile().toString());

        // check if the PDS job result file is as expected
        assertEquals("src/test/resources/config-test-files/valid-files/test-result.txt", pdsJobResult.getFile().toString());
    }

}
