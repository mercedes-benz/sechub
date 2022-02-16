// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.monitoring;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.daimler.sechub.pds.PDSProfiles;
import com.daimler.sechub.pds.PDSShutdownService;
import com.daimler.sechub.pds.config.PDSPathExecutableValidator;
import com.daimler.sechub.pds.config.PDSProductIdentifierValidator;
import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.config.PDSServerConfigurationValidator;
import com.daimler.sechub.pds.config.PDSServerIdentifierValidator;
import com.daimler.sechub.pds.job.PDSJobRepository;

@ActiveProfiles({ PDSProfiles.TEST/* , PDSProfiles.SQL_TRACE */ })
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { PDSPathExecutableValidator.class, PDSServerIdentifierValidator.class, PDSServerConfigurationValidator.class,
        PDSProductIdentifierValidator.class, PDSShutdownService.class, PDSJobRepository.class, PDSServerConfigurationService.class,
        PDSPDSHeartBeatRepositoryDBTest.SimpleTestConfiguration.class })
public class PDSPDSHeartBeatRepositoryDBTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PDSHeartBeatRepository repositoryToTest;

    @Before
    public void before() {
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
