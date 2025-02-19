// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>OAuth2OpaqueTokenIntrospectionResponse</code> represents the response
 * from the OAuth2 opaque token introspection endpoint. It contains various
 * properties related to the token, such as its active status, scope, client ID,
 * client type, username, token type, expiration time, subject, audience, and
 * group type.
 *
 * <p>
 * The <code>active</code> property is required and indicates whether the token
 * is active or not. An inactive token should be treated as
 * <code>401 Unauthorized</code>. For active tokens the <code>sub</code>
 * property is also required. It represents the subject of the authentication
 * request. All other properties are optional and may be <code>null</code>.
 * </p>
 *
 * @author hamidonos
 */
class OAuth2OpaqueTokenIntrospectionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ERR_MSG_FORMAT = "Property '%s' must not be null";

    private static final Duration DEFAULT_EXPIRES_IN = Duration.ofDays(1);

    private static final String JSON_PROPERTY_ACTIVE = "active";
    private static final String JSON_PROPERTY_SCOPE = "scope";
    private static final String JSON_PROPERTY_CLIENT_ID = "client_id";
    private static final String JSON_PROPERTY_CLIENT_TYPE = "client_type";
    private static final String JSON_PROPERTY_USERNAME = "username";
    private static final String JSON_PROPERTY_TOKEN_TYPE = "token_type";
    private static final String JSON_PROPERTY_EXP = "exp";
    private static final String JSON_PROPERTY_SUBJECT = "sub";
    private static final String JSON_PROPERTY_AUDIENCE = "aud";
    private static final String JSON_PROPERTY_GROUP_TYPE = "group_type";

    private final Boolean active;
    private final String scope;
    private final String clientId;
    private final String clientType;
    private final String username;
    private final String tokenType;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final String subject;
    private final String audience;
    private final String groupType;

    /* @formatter:off */
    @JsonCreator
    OAuth2OpaqueTokenIntrospectionResponse(@JsonProperty(JSON_PROPERTY_ACTIVE) Boolean active,
                                           @JsonProperty(JSON_PROPERTY_SCOPE) String scope,
                                           @JsonProperty(JSON_PROPERTY_CLIENT_ID) String clientId,
                                           @JsonProperty(JSON_PROPERTY_CLIENT_TYPE) String clientType,
                                           @JsonProperty(JSON_PROPERTY_USERNAME) String username,
                                           @JsonProperty(JSON_PROPERTY_TOKEN_TYPE) String tokenType,
                                           @JsonProperty(JSON_PROPERTY_EXP) Long expiresAt,
                                           @JsonProperty(JSON_PROPERTY_SUBJECT) String subject,
                                           @JsonProperty(JSON_PROPERTY_AUDIENCE) String audience,
                                           @JsonProperty(JSON_PROPERTY_GROUP_TYPE) String groupType) {
        this.active = requireNonNull(active, ERR_MSG_FORMAT.formatted(JSON_PROPERTY_ACTIVE));
        this.scope = scope;
        this.clientId = clientId;
        this.clientType = clientType;
        this.username = username;
        this.tokenType = tokenType;
        this.issuedAt = Instant.now();
        this.expiresAt = expiresAt != null ? Instant.ofEpochSecond(expiresAt) : null;
        this.subject = active ? requireNonNull(subject, ERR_MSG_FORMAT.formatted(JSON_PROPERTY_SUBJECT)) : subject;
        this.audience = audience;
        this.groupType = groupType;
    }
    /* @formatter:on */

    boolean isActive() {
        return active;
    }

    String getScope() {
        return scope;
    }

    String getClientId() {
        return clientId;
    }

    String getClientType() {
        return clientType;
    }

    String getUsername() {
        return username;
    }

    String getTokenType() {
        return tokenType;
    }

    Instant getIssuedAt() {
        return issuedAt;
    }

    Instant getExpiresAt() {
        return expiresAt;
    }

    String getSubject() {
        return subject;
    }

    String getAudience() {
        return audience;
    }

    String getGroupType() {
        return groupType;
    }
}
