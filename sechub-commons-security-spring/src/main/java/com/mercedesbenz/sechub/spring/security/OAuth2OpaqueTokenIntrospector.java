// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import com.mercedesbenz.sechub.commons.core.cache.CachePersistence;
import com.mercedesbenz.sechub.commons.core.cache.InMemoryCachePersistence;
import com.mercedesbenz.sechub.commons.core.cache.SelfCleaningCache;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;

/**
 * <p>
 * The <code>Base64OAuth2OpaqueTokenIntrospector</code> class is responsible for
 * introspecting opaque OAuth2 tokens. It sends the opaque token via fetcher
 * instance to an introspection endpoint and retrieves the token's details.
 * Afterwards it will do caching.
 * </p>
 *
 * <p>
 * This class integrates with the {@link UserDetailsService} to load user
 * details based on the token's subject. The user details are then used to
 * create an {@link OAuth2IntrospectionAuthenticatedPrincipal} which contains
 * the authenticated principal's information and authorities.
 * </p>
 *
 * <p>
 * The class also handles the encoding of client credentials using Base64 and
 * includes them in the <code>Authorization</code> header of the introspection
 * request.
 * </p>
 *
 * @see org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
 * @see org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see org.springframework.web.client.RestTemplate
 *
 * @author hamidonos
 */
public class OAuth2OpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2OpaqueTokenIntrospector.class);

    private Duration defaultTokenExpiresIn;
    private Duration maxCacheDuration;
    private UserDetailsService userDetailsService;
    private SelfCleaningCache<OAuth2OpaqueTokenIntrospectionResponse> tokenInMemoryCache;
    private SelfCleaningCache<OAuth2OpaqueTokenIntrospectionResponse> tokenClusterCache;
    private OAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcher;

    private OAuth2TokenExpirationCalculator expirationCalculator;
    private Duration minimumTokenValidity;
    private Duration preCacheDuration;

    public static OAuth2OpaqueTokenIntrospectorBuilder builder() {
        return new OAuth2OpaqueTokenIntrospectorBuilder();
    }

    public static class OAuth2OpaqueTokenIntrospectorBuilder {

        private Duration defaultTokenExpiresIn;
        private Duration maxCacheDuration;
        private UserDetailsService userDetailsService;
        private CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> tokenClusterCachePersistence;
        private InMemoryCachePersistence<OAuth2OpaqueTokenIntrospectionResponse> inMemoryCachePersistence;
        private Duration inMemoryCacheClearPeriod;
        private Duration clusterCacheClearPeriod;

        private OAuth2TokenExpirationCalculator expirationCalculator;
        private Duration minimumTokenValidity;
        private Duration preCacheDuration;
        private ApplicationShutdownHandler applicationShutdownHandler;
        private OAuth2OpaqueTokenIDPIntrospectionResponseFetcher introspectionResponseFetcher;

        public OAuth2OpaqueTokenIntrospector build() {
            OAuth2OpaqueTokenIntrospector result = new OAuth2OpaqueTokenIntrospector();
            result.fetcher = requireNonNull(introspectionResponseFetcher, "Parameter fetcher must not be null");

            if (inMemoryCachePersistence == null) {
                inMemoryCachePersistence = new InMemoryCachePersistence<>();
            }
            result.fetcher = introspectionResponseFetcher;

            result.defaultTokenExpiresIn = requireNonNull(defaultTokenExpiresIn, "Parameter defaultTokenExpiresIn must not be null");
            result.maxCacheDuration = requireNonNull(maxCacheDuration, "Parameter maxCacheDuration must not be null");
            result.userDetailsService = requireNonNull(userDetailsService, "Parameter userDetailsService must not be null");
            result.expirationCalculator = requireNonNull(expirationCalculator);

            result.preCacheDuration = requireNonNull(preCacheDuration, "Parameter preCacheDuration must not be null");

            requireNonNull(inMemoryCacheClearPeriod, "Parameter inMemoryCacheClearPeriod must not be null");
            requireNonNull(clusterCacheClearPeriod, "Parameter clusterCacheClearPeriod must not be null");

            result.tokenInMemoryCache = new SelfCleaningCache<>(inMemoryCachePersistence, inMemoryCacheClearPeriod,
                    Executors.newSingleThreadScheduledExecutor(), applicationShutdownHandler);

            if (tokenClusterCachePersistence != null) {
                result.tokenClusterCache = new SelfCleaningCache<>(tokenClusterCachePersistence, clusterCacheClearPeriod,
                        Executors.newSingleThreadScheduledExecutor(), applicationShutdownHandler);

                logger.debug("Created token cluster cache for this application");

            } else {
                logger.info("No token cluster cache created for this application - uses only in memory caching!");
                result.tokenClusterCache = null;
            }

            result.minimumTokenValidity = minimumTokenValidity;

            return result;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setIntrospectionResponseFetcher(
                OAuth2OpaqueTokenIDPIntrospectionResponseFetcher introspectionResponseFetcher) {
            this.introspectionResponseFetcher = introspectionResponseFetcher;
            return this;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setDefaultTokenExpiresIn(Duration defaultTokenExpiresIn) {
            this.defaultTokenExpiresIn = defaultTokenExpiresIn;
            return this;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setMaxCacheDuration(Duration maxCacheDuration) {
            this.maxCacheDuration = maxCacheDuration;
            return this;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setUserDetailsService(UserDetailsService userDetailsService) {
            this.userDetailsService = userDetailsService;
            return this;
        }

        /**
         * Set in cluster cache persistence instance. If null or this setter is not
         * called, no token cluster cache mechanism is used.
         *
         * @param inMemoryCachePersistence, can be <code>null</code>
         * @return builder
         */
        public OAuth2OpaqueTokenIntrospectorBuilder setTokenClusterCachePersistence(
                CachePersistence<OAuth2OpaqueTokenIntrospectionResponse> tokenClusterCachePersistence) {
            this.tokenClusterCachePersistence = tokenClusterCachePersistence;
            return this;
        }

        /**
         * Set in memory cache persistence instance. If null or this setter is not
         * called, a new in memory cache persistence will be created automatically
         *
         * @param inMemoryCachePersistence
         * @return builder
         */
        public OAuth2OpaqueTokenIntrospectorBuilder setTokenInMemoryCachePersistence(
                InMemoryCachePersistence<OAuth2OpaqueTokenIntrospectionResponse> inMemoryCachePersistence) {
            this.inMemoryCachePersistence = inMemoryCachePersistence;
            return this;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setInMemoryCacheClearPeriod(Duration inMemoryCacheClearPeriod) {
            this.inMemoryCacheClearPeriod = inMemoryCacheClearPeriod;
            return this;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setClusterCacheClearPeriod(Duration clusterCacheClearPeriod) {
            this.clusterCacheClearPeriod = clusterCacheClearPeriod;
            return this;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setExpirationCalculator(OAuth2TokenExpirationCalculator expirationCalculator) {
            this.expirationCalculator = expirationCalculator;
            return this;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setMinimumTokenValidity(Duration minimumTokenValidity) {
            this.minimumTokenValidity = minimumTokenValidity;
            return this;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setPreCacheDuration(Duration preCacheDuration) {
            this.preCacheDuration = preCacheDuration;
            return this;
        }

        public OAuth2OpaqueTokenIntrospectorBuilder setApplicationShutdownHandler(ApplicationShutdownHandler applicationShutdownHandler) {
            this.applicationShutdownHandler = applicationShutdownHandler;
            return this;
        }
    }

    /** Private constructor - to ensure only created by builder */
    private OAuth2OpaqueTokenIntrospector() {

    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String opaqueToken) throws OAuth2AuthenticationException {
        if (opaqueToken == null || opaqueToken.isEmpty()) {
            throw new BadOpaqueTokenException("Token is null or empty");
        }

        Instant now = Instant.now();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = getIntrospectionResponseFromInMemoryCache(opaqueToken, now);

        if (expirationCalculator.isExpired(introspectionResponse, now)) {
            /*
             * Remove value from cache (we differ between expiration and cache time and want
             * no orphans in cache).
             */
            tokenInMemoryCache.remove(opaqueToken);
            if (tokenClusterCache != null) {
                tokenClusterCache.remove(opaqueToken);
            }

            /*
             * Remark: Spring Boot will catch any BadOpaqueTokenException inside
             * OpaqueTokenAuthenticationProvider and transform it to an
             * InvalidBearerTokenException which will be fetched and handled by our custom
             * Authentication entry point inside AbstractSecurityConfiguration, leading to a
             * remove of the oauth2 cookie at browser side and finally to a 401 unauthorized
             * response.
             */
            throw new BadOpaqueTokenException("Opaque token is expired");
        }

        if (!introspectionResponse.isActive()) {
            throw new BadOpaqueTokenException("Token is not active");
        }

        String subject = introspectionResponse.getSubject();

        if (subject == null || subject.isEmpty()) {
            throw new BadOpaqueTokenException("Subject is null or empty");
        }

        Map<String, Object> introspectionClaims = getIntrospectionClaims(introspectionResponse, now);
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
        Collection<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
        String username = userDetails.getUsername();

        return new OAuth2IntrospectionAuthenticatedPrincipal(username, introspectionClaims, authorities);
    }

    private OAuth2OpaqueTokenIntrospectionResponse getIntrospectionResponseFromInMemoryCache(String opaqueToken, Instant now) {
        /* @formatter:off */

        Optional<OAuth2OpaqueTokenIntrospectionResponse> fromInMemoryCache = tokenInMemoryCache.get(opaqueToken);
        if (fromInMemoryCache.isPresent()) {
            logger.trace("found introspection response in memory for token: {}", opaqueToken);
            return fromInMemoryCache.get();
        }
        /* not found in memory - fetch from cluster cache/or from IDP */
        OAuth2OpaqueTokenIntrospectionResponse resolved = getIntrospectionResponseFromClusterCache(opaqueToken, now);
        /* update cache: */
        updateInMemoryCache(opaqueToken, resolved, now);

        return resolved;
        /* @formatter:on */
    }

    private OAuth2OpaqueTokenIntrospectionResponse getIntrospectionResponseFromClusterCache(String opaqueToken, Instant now) {
        if (tokenClusterCache == null) {
            /* no cluster cache defined - just get the value from IDP */
            logger.trace("no token cluster cache deifned - skip");
            return fetchAndCacheTokenIntrospectionResponseFromIDP(opaqueToken, now);
        }

        Optional<OAuth2OpaqueTokenIntrospectionResponse> fromClusterCache = tokenClusterCache.get(opaqueToken);
        if (fromClusterCache.isPresent()) {
            logger.trace("found introspection response in cluster cache for token: {}", opaqueToken);
            return fromClusterCache.get();
        }
        OAuth2OpaqueTokenIntrospectionResponse resolved = fetchAndCacheTokenIntrospectionResponseFromIDP(opaqueToken, now);
        updateDatabaseCache(opaqueToken, resolved, now);

        return resolved;
    }

    private OAuth2OpaqueTokenIntrospectionResponse fetchAndCacheTokenIntrospectionResponseFromIDP(String opaqueToken, Instant now) {
        logger.trace("start fetching introspection response from IDP");
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = fetcher.fetchOpaqueTokenIntrospectionFromIDP(opaqueToken);

        if (introspectionResponse == null) {
            throw new BadOpaqueTokenException("Token introspection response from IDP is null");
        }

        handleDefaultAndMiniumExpiration(now, introspectionResponse);
        return introspectionResponse;
    }

    private void handleDefaultAndMiniumExpiration(Instant now, OAuth2OpaqueTokenIntrospectionResponse introspectionResponse) {
        if (introspectionResponse.getExpiresAtAsInstant() == null) {

            Instant calculatedExpiresAtWithDefault = now.plus(defaultTokenExpiresIn);
            introspectionResponse.setExpiresAt(calculatedExpiresAtWithDefault.getEpochSecond());

            logger.debug("Opaque token introspection response did not contain an `expiresAt` entry! Set `expiresAt` to calculated value `{}` as fallback.",
                    calculatedExpiresAtWithDefault);
        }
        if (minimumTokenValidity != null) {
            Instant minimumTokenValidityInstant = now.plus(minimumTokenValidity);
            if (minimumTokenValidityInstant.isAfter(introspectionResponse.getExpiresAtAsInstant())) {
                introspectionResponse.setExpiresAt(minimumTokenValidityInstant.getEpochSecond());

                logger.debug(
                        "Opaque token 'expiresAt' entry was smaller than the configured 'minimumTokenValidity'! Set 'expiresAt' to configured 'minimumTokenValidity' value '{}' as fallback.",
                        minimumTokenValidityInstant);
            }
        }
    }

    private void updateDatabaseCache(String opaqueToken, OAuth2OpaqueTokenIntrospectionResponse introspectionResponse, Instant now) {
        if (tokenClusterCache == null) {
            logger.trace("no token cluster cache available - skip update");
            return;
        }
        Duration idpBasedCacheDurationInSeconds = calculateIDPbasedCacheDurationInSeconds(introspectionResponse, now);
        /* Update cluster cache, use IDP cache duration */
        tokenClusterCache.put(opaqueToken, introspectionResponse, idpBasedCacheDurationInSeconds);
        logger.trace("token cluster cache updated for token: {}", opaqueToken);
    }

    private void updateInMemoryCache(String opaqueToken, OAuth2OpaqueTokenIntrospectionResponse introspectionResponse, Instant now) {

        if (tokenClusterCache == null) {
            Duration idpBasedCacheDurationInSeconds = calculateIDPbasedCacheDurationInSeconds(introspectionResponse, now);
            /*
             * No cluster cache available - here we have only the in memory cache and use
             * IDP cache duration
             */
            tokenInMemoryCache.put(opaqueToken, introspectionResponse, idpBasedCacheDurationInSeconds);
        } else {
            /*
             * Here the in memory cache is only for short time (to prevent always asking
             * cluster cache ). So we use NOT IDP cache duration but the shortCacheDuration
             * field
             */
            tokenInMemoryCache.put(opaqueToken, introspectionResponse, preCacheDuration);
            logger.trace("in memory cache updated for token: {}", opaqueToken);
        }
    }

    private Duration calculateIDPbasedCacheDurationInSeconds(OAuth2OpaqueTokenIntrospectionResponse introspectionResponse, Instant now) {
        Instant idpCacheExpiresAtInSeconds = introspectionResponse.getExpiresAtAsInstant();

        // sanity check
        if (idpCacheExpiresAtInSeconds == null) {
            throw new IllegalStateException("May not happen - must be handled with defaults before!");
        }

        Duration idpBasedCacheDurationInSeconds = Duration.between(now.truncatedTo(ChronoUnit.SECONDS), idpCacheExpiresAtInSeconds);

        // we use only the calculated duration for the cluster variant!

        if (maxCacheDuration.compareTo(idpBasedCacheDurationInSeconds) < 0) {
            logger.debug("Opaque token cache duration of %s exceeds the maximum cache duration of %s. Using the maximum cache duration instead."
                    .formatted(idpBasedCacheDurationInSeconds.toString(), maxCacheDuration.toString()));
            idpBasedCacheDurationInSeconds = maxCacheDuration;
        }
        return idpBasedCacheDurationInSeconds;
    }

    /* @formatter:off */
    private Map<String, Object> getIntrospectionClaims(OAuth2OpaqueTokenIntrospectionResponse oAuth2OpaqueTokenIntrospectionResponse, Instant now) {
        Map<String, Object> map = new HashMap<>();
        map.put(OAuth2TokenIntrospectionClaimNames.ACTIVE, oAuth2OpaqueTokenIntrospectionResponse.isActive());
        map.put(OAuth2TokenIntrospectionClaimNames.SCOPE, oAuth2OpaqueTokenIntrospectionResponse.getScope());
        map.put(OAuth2TokenIntrospectionClaimNames.CLIENT_ID, oAuth2OpaqueTokenIntrospectionResponse.getClientId());
        map.put(OAuth2TokenIntrospectionClaimNames.USERNAME, oAuth2OpaqueTokenIntrospectionResponse.getUsername());
        map.put(OAuth2TokenIntrospectionClaimNames.TOKEN_TYPE, oAuth2OpaqueTokenIntrospectionResponse.getTokenType());
        map.put(OAuth2TokenIntrospectionClaimNames.IAT, oAuth2OpaqueTokenIntrospectionResponse.getIssuedAt());
        Instant expiresAt = oAuth2OpaqueTokenIntrospectionResponse.getExpiresAtAsInstant();
        if (expiresAt == null) {
            expiresAt = now.plus(defaultTokenExpiresIn);
        }
        map.put(OAuth2TokenIntrospectionClaimNames.EXP, expiresAt);
        map.put(OAuth2TokenIntrospectionClaimNames.SUB, oAuth2OpaqueTokenIntrospectionResponse.getSubject());
        map.put(OAuth2TokenIntrospectionClaimNames.AUD, oAuth2OpaqueTokenIntrospectionResponse.getAudience());
        return map;
    }
    /* @formatter:on */
}