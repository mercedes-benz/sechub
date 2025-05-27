// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.commons.core.cache.CacheData;
import com.mercedesbenz.sechub.commons.core.cache.InMemoryCachePersistence;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;
import com.mercedesbenz.sechub.commons.core.shutdown.ShutdownListener;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.security.clustercache.OAuth2OpaqueTokenClusterCachePersistence;
import com.mercedesbenz.sechub.spring.security.AbstractSecurityConfiguration;
import com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIDPIntrospectionResponseFetcher;
import com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIntrospectionResponse;
import com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider;
import com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIntrospector;
import com.mercedesbenz.sechub.spring.security.OAuth2TokenExpirationCalculator;

/**
 * This test controller was introduced to have a possibility to test cluster
 * cache mechanism in integration tests directly without calling a real IDP
 * provider.
 *
 * The is an emulation of things configured inside
 * {@link AbstractSecurityConfiguration} when it comes to oauth2 introspection.
 *
 */
@RestController
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestOAuth2TokenTestCacherRestController {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestOAuth2TokenTestCacherRestController.class);

    private static final String USER_TOKEN1_LONG_IN_MEMORY = "user-token1-long-in-memory";

    private static final String USER_TOKEN2_SHORT_IN_MEMORY = "user-token2-short-in-memory";
    private static final String USER_TOKEN2_LONG_IN_CLUSTER_CACHE = "user-token2-long-in-cluster-cache";

    private static final String USER_TOKEN3_SHORT_IN_MEMORY = "user-token3-short-in-memory";
    private static final String USER_TOKEN3_SHORT_IN_CLUSTER_CACHE = "user-token3-short-in-cluster-cache";

    private static final String TEST_TOKEN_1 = "TEST-TOKEN-1";
    private static final String TEST_TOKEN_2 = "TEST-TOKEN-2";
    private static final String TEST_TOKEN_3 = "TEST-TOKEN-3";
    private static final String TEST_TOKEN_4 = "TEST-TOKEN-4"; // used in test
    private static final String TEST_TOKEN_5 = "TEST-TOKEN-5"; // used in test
    private static final String TEST_TOKEN_6 = "TEST-TOKEN-6"; // used in test

    private static final String INTEGRATIONTEST_CACHING_OPAQUE_TOKEN = APIConstants.API_ANONYMOUS + "integrationtest/caching/opaque-token/";
    private static final String INTEGRATIONTEST_CACHING_OPAQUE_TOKEN_INTROSPECT = INTEGRATIONTEST_CACHING_OPAQUE_TOKEN + "introspect";
    private static final String INTEGRATIONTEST_CACHING_OPAQUE_TOKEN_INIT_TEST_CACHE = INTEGRATIONTEST_CACHING_OPAQUE_TOKEN + "init-test-cache";
    private static final String INTEGRATIONTEST_CACHING_OPAQUE_TOKEN_SHUTDOWN_TEST_CACHE = INTEGRATIONTEST_CACHING_OPAQUE_TOKEN + "shutdown-test-cache";

    @Autowired
    OAuth2OpaqueTokenClusterCachePersistence tokenClusterCachePersistence;

    @Autowired
    OAuth2TokenExpirationCalculator expirationCalculator;

    @Autowired
    OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider cryptoAccessProvider;

    private TestCleanupHandler testShutdownHandler;
    private OAuth2OpaqueTokenIDPIntrospectionResponseFetcher testFetcher;
    private InMemoryCachePersistence<OAuth2OpaqueTokenIntrospectionResponse> testInMemoryPersistence;
    private OAuth2OpaqueTokenIntrospector opaqueTokenIntrospector;
    private TestUserDetailsService testUserDetailsService;

    /**
     * This initializes test setup
     */
    @RequestMapping(path = INTEGRATIONTEST_CACHING_OPAQUE_TOKEN_INIT_TEST_CACHE, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void initTestCache() {
        logger.debug("init test cache");

        setupCacheTestEnvironment();

        prepareInitialCacheTestData();

    }

    private void setupCacheTestEnvironment() {
        testUserDetailsService = new TestUserDetailsService();
        testShutdownHandler = new TestCleanupHandler();
        testFetcher = new TestIDPIntrospectionResponseFetcher();
        testInMemoryPersistence = new InMemoryCachePersistence<>();

        /* @formatter:off */
        opaqueTokenIntrospector = OAuth2OpaqueTokenIntrospector.builder().
                setCryptoAccessProvider(cryptoAccessProvider).
                setTokenInMemoryCachePersistence(testInMemoryPersistence).
                setIntrospectionResponseFetcher(testFetcher).
                setDefaultTokenExpiresIn(Duration.ofMinutes(66)).
                setMaxCacheDuration(Duration.ofDays(1)).
                setPreCacheDuration(Duration.ofSeconds(10)).
                setInMemoryCacheClearPeriod(Duration.ofMillis(200)). // make in memory cache cleanup check time very short
                setClusterCacheClearPeriod(Duration.ofMillis(200)). // make in cluster cache cleanup check time very short
                setUserDetailsService(testUserDetailsService).
                setApplicationShutdownHandler(testShutdownHandler).
                setExpirationCalculator(expirationCalculator).
                setTokenClusterCachePersistence(tokenClusterCachePersistence).
                setMinimumTokenValidity(Duration.ofSeconds(20)).
        build();
        /* @formatter:on */
    }

    private void prepareInitialCacheTestData() {
        /* prepare data */
        Instant now = Instant.now();

        OAuth2OpaqueTokenIntrospectionResponse response1_long_im = createFakedIDPOpaqueTokenResponse(USER_TOKEN1_LONG_IN_MEMORY, Duration.ofSeconds(10));

        OAuth2OpaqueTokenIntrospectionResponse response2_short_im = createFakedIDPOpaqueTokenResponse(USER_TOKEN2_SHORT_IN_MEMORY, Duration.ofSeconds(30));
        OAuth2OpaqueTokenIntrospectionResponse response2_long_cc = createFakedIDPOpaqueTokenResponse(USER_TOKEN2_LONG_IN_CLUSTER_CACHE, Duration.ofSeconds(30));

        OAuth2OpaqueTokenIntrospectionResponse response3_short_im = createFakedIDPOpaqueTokenResponse(USER_TOKEN3_SHORT_IN_MEMORY, Duration.ofHours(1));
        OAuth2OpaqueTokenIntrospectionResponse response3_short_cc = createFakedIDPOpaqueTokenResponse(USER_TOKEN3_SHORT_IN_CLUSTER_CACHE, Duration.ofHours(1));

        CacheData<OAuth2OpaqueTokenIntrospectionResponse> data1_long_im = createCacheData(response1_long_im, Duration.ofHours(10), now);

        CacheData<OAuth2OpaqueTokenIntrospectionResponse> data2_short_im = createCacheData(response2_short_im, Duration.ofMillis(50), now);
        CacheData<OAuth2OpaqueTokenIntrospectionResponse> data2_long_cc = createCacheData(response2_long_cc, Duration.ofHours(20), now);

        CacheData<OAuth2OpaqueTokenIntrospectionResponse> data3_short_im = createCacheData(response3_short_im, Duration.ofMillis(50), now);
        CacheData<OAuth2OpaqueTokenIntrospectionResponse> data3_short_cc = createCacheData(response3_short_cc, Duration.ofMillis(100), now);

        /* prepare test cache data */
        testInMemoryPersistence.put(TEST_TOKEN_1, data1_long_im);
        // we do not set something in cluster cache

        testInMemoryPersistence.put(TEST_TOKEN_2, data2_short_im);
        tokenClusterCachePersistence.put(TEST_TOKEN_2, data2_long_cc);

        testInMemoryPersistence.put(TEST_TOKEN_3, data3_short_im);
        tokenClusterCachePersistence.put(TEST_TOKEN_3, data3_short_cc);
    }

    private CacheData<OAuth2OpaqueTokenIntrospectionResponse> createCacheData(OAuth2OpaqueTokenIntrospectionResponse response1_long_im, Duration ofHours,
            Instant createdAt) {
        return new CacheData<OAuth2OpaqueTokenIntrospectionResponse>(response1_long_im, ofHours, cryptoAccessProvider, createdAt);
    }

    @RequestMapping(path = INTEGRATIONTEST_CACHING_OPAQUE_TOKEN_INTROSPECT + "/{opaqueToken}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public String introspect(@PathVariable("opaqueToken") String opaqueToken) {
        logger.debug("introspect opaque token: {}", opaqueToken);
        OAuth2AuthenticatedPrincipal result = opaqueTokenIntrospector.introspect(opaqueToken);
        if (logger.isTraceEnabled()) {
            logger.trace("introspector result contains: attributes.exp=:{}", result.getAttribute("exp").toString());
        }
        return JSONConverter.get().toJSON(result);
    }

    @RequestMapping(path = INTEGRATIONTEST_CACHING_OPAQUE_TOKEN_SHUTDOWN_TEST_CACHE, method = RequestMethod.POST, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void shutdownTestCache() {
        logger.debug("trigger shutdown to test caches");
        testShutdownHandler.cleanup(); // this will shutdown the self cleaning caches (in memory + cluster cache) - we
                                       // do not need the any longer.
    }

    private static OAuth2OpaqueTokenIntrospectionResponse createFakedIDPOpaqueTokenResponse(String username, Duration expiration) {

        /* @formatter:off */
        Instant expiresAtInstant = expiration!=null ? Instant.now().plus(expiration) :null;
        Long expiresAt = expiresAtInstant != null ? expiresAtInstant.getEpochSecond(): null;

        logger.trace("duration={} leads to expiresAtInstant:{}", expiration, expiresAtInstant, expiresAt);
        return new OAuth2OpaqueTokenIntrospectionResponse(
                true,
                "scope",
                "client-id",
                "client-type",
                username,
                "token-type",
                expiresAt,
                "subject",
                "aud",
                "group-type");
        /* @formatter:on */
    }

    private static class TestIDPIntrospectionResponseFetcher implements OAuth2OpaqueTokenIDPIntrospectionResponseFetcher {

        @Override
        public OAuth2OpaqueTokenIntrospectionResponse fetchOpaqueTokenIntrospectionFromIDP(String opaqueToken) {
            Duration duration = null;
            if (TEST_TOKEN_5.equals(opaqueToken)) {
                duration = Duration.of(4, ChronoUnit.SECONDS); // to have an example where minimum validity is used
            } else if (TEST_TOKEN_6.equals(opaqueToken)) {
                duration = null;
            } else {
                duration = Duration.of(2, ChronoUnit.HOURS);
            }
            return createFakedIDPOpaqueTokenResponse("user-fetched-from-idp", duration);
        }

    }

    private class TestCleanupHandler implements ApplicationShutdownHandler {

        private List<ShutdownListener> shutdownListeners = new ArrayList<>();

        @Override
        public void register(ShutdownListener shutdownListener) {
            shutdownListeners.add(shutdownListener);
        }

        public void cleanup() {
            for (ShutdownListener shutdownListener : shutdownListeners) {
                shutdownListener.onShutdown();
            }
            removeFromCaches(TEST_TOKEN_1);
            removeFromCaches(TEST_TOKEN_2);
            removeFromCaches(TEST_TOKEN_3);
            removeFromCaches(TEST_TOKEN_4);
            removeFromCaches(TEST_TOKEN_5);
        }

        private void removeFromCaches(String opaqueToken) {
            tokenClusterCachePersistence.remove(opaqueToken);
            testInMemoryPersistence.remove(opaqueToken);
        }
    }

    private class JustUsernameTestUserDetails implements UserDetails {

        private static final long serialVersionUID = 1L;
        private String username;

        public JustUsernameTestUserDetails(String username) {
            this.username = username;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.emptyList(); // not relevant
        }

        @Override
        public String getPassword() {
            return "not-relevant";
        }

        @Override
        public String getUsername() {
            return username;
        }

    }

    private class TestUserDetailsService implements UserDetailsService {

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

            return new JustUsernameTestUserDetails(username);
        }

    }
}
