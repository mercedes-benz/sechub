// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationTypeListParser;
import com.mercedesbenz.sechub.pds.PDSShutdownService;
import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;
import com.mercedesbenz.sechub.pds.config.PDSConfigurationAutoFix;
import com.mercedesbenz.sechub.pds.config.PDSPathExecutableValidator;
import com.mercedesbenz.sechub.pds.config.PDSProductIdentifierValidator;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationValidator;
import com.mercedesbenz.sechub.pds.config.PDSServerIdentifierValidator;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;

@ActiveProfiles({ PDSProfiles.TEST/* , PDSProfiles.SQL_TRACE */ })
@DataJpaTest
@ContextConfiguration(classes = { PDSPathExecutableValidator.class, PDSServerIdentifierValidator.class, PDSServerConfigurationValidator.class,
        PDSProductIdentifierValidator.class, PDSShutdownService.class, PDSJobRepository.class, PDSServerConfigurationService.class,
        PDSPDSHeartBeatRepositoryDBTest.SimpleTestConfiguration.class, PDSConfigurationAutoFix.class, SecHubDataConfigurationTypeListParser.class })
public class PDSPDSHeartBeatRepositoryDBTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PDSHeartBeatRepository repositoryToTest;

    @BeforeEach
    public void beforeEach() {
    }

    @Test
    public void create_heartbeat() {
        /* prepare */
        PDSHeartBeat heartBeat = createHeartBeat();

        /* execute */
        PDSHeartBeat heartbeat = repositoryToTest.save(heartBeat);

        /* test */
        assertNotNull(heartbeat.getUUID());
    }

    @Test
    public void create_heartbeat_set_to_old_timestamp_and_delete__old_entry_is_removed() {
        /* prepare */
        PDSHeartBeat heartBeat = createHeartBeat();
        LocalDateTime oldUpdated = LocalDateTime.now().minusHours(3);
        heartBeat.setUpdated(oldUpdated);

        heartBeat = entityManager.persistAndFlush(heartBeat);
        UUID uuid = heartBeat.getUUID();

        /* check precondition */
        Optional<PDSHeartBeat> found = repositoryToTest.findById(uuid);
        assertTrue(found.isPresent());
        assertEquals(oldUpdated, found.get().getUpdated());

        /* execute */
        repositoryToTest.removeOlderThan(LocalDateTime.now().minusHours(2));

        /* test */
        entityManager.detach(heartBeat); // detach this object, so its no longer cached but reloaded
        found = repositoryToTest.findById(uuid);
        assertFalse(found.isPresent());
    }

    private PDSHeartBeat createHeartBeat() {
        PDSHeartBeat result = new PDSHeartBeat();
        result.setServerId("server-id");
        return result;
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
