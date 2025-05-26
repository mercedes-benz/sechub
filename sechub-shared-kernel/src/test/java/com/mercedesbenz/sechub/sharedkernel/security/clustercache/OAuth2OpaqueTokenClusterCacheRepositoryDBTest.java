// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security.clustercache;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = { OAuth2OpaqueTokenClusterCacheRepositoryDBTest.SimpleTestConfiguration.class, OAuth2OpaqueTokenClusterCache.class })
class OAuth2OpaqueTokenClusterCacheRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OAuth2OpaqueTokenClusterCacheRepository repositoryToTest;

    @Test
    void deleteById_works_as_expected() {
        /* prepare */
        Instant now = Instant.now();
        String opaqueToken = "pseudo-token-" + System.nanoTime();
        String introSpectionResponse = "something";

        OAuth2OpaqueTokenClusterCache entity = new OAuth2OpaqueTokenClusterCache(opaqueToken, introSpectionResponse, Duration.ofMinutes(2), now);

        entityManager.persistAndFlush(entity);

        /* check precondition: can be found */
        assertThat(repositoryToTest.existsById(opaqueToken)).isTrue();

        /* execute */
        repositoryToTest.deleteById(opaqueToken);

        /* test */
        assertThat(repositoryToTest.existsById(opaqueToken)).isFalse();

    }

    @Test
    void deleteOutdated_works_as_expected_when_outdated() {
        /* prepare */
        Instant now = Instant.now();
        String opaqueToken = "pseudo-token-" + System.nanoTime();
        String introSpectionResponse = "something";

        OAuth2OpaqueTokenClusterCache entity = new OAuth2OpaqueTokenClusterCache(opaqueToken, introSpectionResponse, Duration.ofMinutes(2), now);

        entityManager.persistAndFlush(entity);

        /* check precondition: can be found */
        assertThat(repositoryToTest.existsById(opaqueToken)).isTrue();

        /* execute */
        repositoryToTest.removeOutdated(now.plus(Duration.ofMinutes(3)));

        /* test */
        assertThat(repositoryToTest.existsById(opaqueToken)).isFalse();

    }

    @Test
    void deleteOutdated_works_as_expected_when_not_outdated() {
        /* prepare */
        Instant now = Instant.now();
        String opaqueToken = "pseudo-token-" + System.nanoTime();
        String introSpectionResponse = "something";

        OAuth2OpaqueTokenClusterCache entity = new OAuth2OpaqueTokenClusterCache(opaqueToken, introSpectionResponse, Duration.ofMinutes(2), now);

        entityManager.persistAndFlush(entity);

        /* check precondition: can be found */
        assertThat(repositoryToTest.existsById(opaqueToken)).isTrue();

        /* execute */
        repositoryToTest.removeOutdated(now.plus(Duration.ofMinutes(1)));

        /* test */
        assertThat(repositoryToTest.existsById(opaqueToken)).isTrue();

    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
