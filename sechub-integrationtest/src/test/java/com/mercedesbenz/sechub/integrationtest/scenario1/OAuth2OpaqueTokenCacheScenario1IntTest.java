// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI.TestOAuth2AuthenticatedPrincipal;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario1.class)
public class OAuth2OpaqueTokenCacheScenario1IntTest {

    private static final String USER_TOKEN_NOT_FROM_CACHE_BUT_FROM_IDP = "user-fetched-from-idp";
    private static final String USER_TOKEN1_LONG_IN_MEMORY = "user-token1-long-in-memory";

    private static final String USER_TOKEN2_LONG_IN_CLUSTER_CACHE = "user-token2-long-in-cluster-cache";

    @BeforeAll
    static void beforeAll() {
        /* @formatter:off */
         /* Next line initializes a test cache with following test setup:
         *
         * - Precache-Duration: 10 seconds (for in memory cache)
         * - Default token expires 66 Minutes.
         *
         * - in memory cache clear period 200 ms
         * - cluster cache clear period 200 ms
         * - minimum token validity is set to 20 seconds.
         *
         * The faked IDP response will
         * - always contain user name "user-fetched-from-idp" (if names differ in tests, this
         *   comes from fix values in cache)
         *
         * The caches are already filled with values. The cache time is different here for entries which
         * cannot happen in "real life". But for testing we have changed this in initialized test data:
         *
         * <pre>
         *                  Cache-Time before clean     Cache-Time before clean    Cache Token data      IDP       Expected resulting
         * Opaque token     In-memory-cache             cluster-cache              expiresAt          Token exp    Token validity
         * ---------------------------------------------------------------------------------------------------------------------------------
         * TEST-TOKEN-1     10 hours                    -                           10 seconds          2 hours     10 seconds (smaller than minimum, but was directly injected by test to cache...)
         * TEST-TOKEN-2     50 milliseconds             20 hours                    30 seconds         2 hours      30 seconds
         * TEST-TOKEN-3     50 milliseconds             100 milliseconds            1 hour             2 hours      2 hours
         * TEST-TOKEN-4     -                           -                           -                  2 hours      2 hours
         * TEST-TOKEN-5     -                           -                           -                  4 seconds    20 seconds (from minimum)
         * TEST-TOKEN-6     -                           -                           -                  null         66 minutes (from default)
         * </pre>
         *
         * The in memory cache is cleared every 200 milliseconds
         * The in cluster cache is cleared every 200 milliseconds
         *
         * For details look into IntegrationTestOAuth2TokenTestCacherRestController.java
         *
         * */
        /* @formatter:on */
        TestAPI.initOpaqueTokenTestCache();
    }

    @AfterAll
    static void afterAll() {
        // We reset test cache. So we can restart the test again and again:
        // will shutdown old self cleaning caches + removes data from persistence
        TestAPI.shutdownOpaqueTokenTestCache();
    }

    @Test
    void token1__is_still_inside_memory_cache() {
        /* @formatter:off */

        /* execution */
        TestOAuth2AuthenticatedPrincipal result = TestAPI.introspectOpaqueTokenTestCache("TEST-TOKEN-1");

        /* test */
        assertThat(result.getName()).isEqualTo("subject");
        assertThat(result.getUsername()).isEqualTo(USER_TOKEN1_LONG_IN_MEMORY);
        assertThat(result.isActive()).isTrue();

        assertExpirationIsEqualTo(result, Duration.ofSeconds(10));

	    /* @formatter:on */
    }

    @Test
    void token2_exists_no_longer_in_memory_but_is_kept_in_cluster_cache() {

        /* execution */
        TestOAuth2AuthenticatedPrincipal result = TestAPI.introspectOpaqueTokenTestCache("TEST-TOKEN-2");

        /* test */
        assertThat(result.getName()).isEqualTo("subject");
        assertThat(result.getUsername()).isEqualTo(USER_TOKEN2_LONG_IN_CLUSTER_CACHE);
        assertThat(result.isActive()).isTrue();

        assertExpirationIsEqualTo(result, Duration.ofSeconds(30));

    }

    @Test
    void token3__exists_in_cache_but_cleaned_in_both_caches_so_idp_result_will_be_fetched_again() {

        /* execution */
        TestOAuth2AuthenticatedPrincipal result = TestAPI.introspectOpaqueTokenTestCache("TEST-TOKEN-3");

        /* test */
        assertThat(result.getName()).isEqualTo("subject");
        assertThat(result.getUsername()).isEqualTo(USER_TOKEN_NOT_FROM_CACHE_BUT_FROM_IDP);
        assertThat(result.isActive()).isTrue();

        assertExpirationIsEqualTo(result, Duration.ofHours(2));

    }

    @Test
    void token4_not_existing_cache_fetched_by_idp_expires_in_two_hours() {

        /* execution */
        TestOAuth2AuthenticatedPrincipal result = TestAPI.introspectOpaqueTokenTestCache("TEST-TOKEN-4");

        /* test */
        assertThat(result.getName()).isEqualTo("subject");
        assertThat(result.getUsername()).isEqualTo(USER_TOKEN_NOT_FROM_CACHE_BUT_FROM_IDP);
        assertThat(result.isActive()).isTrue();

        assertExpirationIsEqualTo(result, Duration.ofHours(2));

    }

    @Test
    void token5_not_existing_cache_fetched_by_idp_would_expires_in_4_seconds__to_low__minimum_validity_will_be_set() {

        /* execution */
        TestOAuth2AuthenticatedPrincipal result = TestAPI.introspectOpaqueTokenTestCache("TEST-TOKEN-5");

        /* test */
        assertThat(result.getName()).isEqualTo("subject");
        assertThat(result.getUsername()).isEqualTo(USER_TOKEN_NOT_FROM_CACHE_BUT_FROM_IDP);
        assertThat(result.isActive()).isTrue();

        assertExpirationIsEqualTo(result, Duration.ofSeconds(20));

    }

    @Test
    void token6_not_existing_cache_fetched_by_idp_no_expiresAt_set__default_value_from_setup_is_used() {

        /* execution */
        TestOAuth2AuthenticatedPrincipal result = TestAPI.introspectOpaqueTokenTestCache("TEST-TOKEN-6");

        /* test */
        assertThat(result.getName()).isEqualTo("subject");
        assertThat(result.getUsername()).isEqualTo(USER_TOKEN_NOT_FROM_CACHE_BUT_FROM_IDP);
        assertThat(result.isActive()).isTrue();

        assertExpirationIsEqualTo(result, Duration.ofMinutes(66));

    }

    private void assertExpirationIsEqualTo(TestOAuth2AuthenticatedPrincipal result, Duration durationFromNow) {
        String expirationAsString = (String) result.getExpiresAt();

        String issuedAt = result.getIssuedAt();

        Instant expiration = Instant.parse(expirationAsString);
        Instant creation = Instant.parse(issuedAt);
        assertExpirationIsEqualTo(expiration, creation, durationFromNow);
    }

    private void assertExpirationIsEqualTo(Instant expiration, Instant creation, Duration expectedDuration) {

        Instant calculated = creation.plus(expectedDuration);

        Duration foundDuration = Duration.between(creation, expiration);

        long durationDiffSeconds = foundDuration.toSeconds() - expectedDuration.toSeconds();

        // we use 2 seconds to test diff failures. This avoids flaky tests and is still
        // enough for these test and the defined test data.
        if (durationDiffSeconds > 2 || durationDiffSeconds < -2) {
            String message = """
                    Expiration failure detected in introspection result:

                    Duration expexted  : %s
                    Found Duration     : %s
                    -->Diff in seconds : %s

                    Creation           : %s
                    Expiration         : %s
                    Calculated         : %s


                    """.formatted(expectedDuration, foundDuration, durationDiffSeconds, creation, expiration, calculated);

            fail(message);
        }
    }

}
