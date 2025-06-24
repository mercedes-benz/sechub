// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

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
public class OAuth2OpaqueTokenIntrospectionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ERR_MSG_FORMAT = "Property '%s' must not be null";

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

    private Long expiresAt; // not final, because we may change this to fallback value if null
    private Instant expiresAtInstant;

    private final String subject;
    private final String audience;
    private final String groupType;

    /* @formatter:off */
    @JsonCreator
    public OAuth2OpaqueTokenIntrospectionResponse(@JsonProperty(JSON_PROPERTY_ACTIVE) Boolean active,
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
        this.expiresAt = expiresAt;
        this.subject = active ? requireNonNull(subject, ERR_MSG_FORMAT.formatted(JSON_PROPERTY_SUBJECT)) : subject;
        this.audience = audience;
        this.groupType = groupType;

        calculateInitExpireAsInstant();
    }
    /* @formatter:on */

    @JsonProperty(JSON_PROPERTY_ACTIVE)
    public Boolean isActive() {
        return active;
    }

    @JsonProperty(JSON_PROPERTY_SCOPE)
    public String getScope() {
        return scope;
    }

    @JsonProperty(JSON_PROPERTY_CLIENT_ID)
    public String getClientId() {
        return clientId;
    }

    @JsonProperty(JSON_PROPERTY_CLIENT_TYPE)
    public String getClientType() {
        return clientType;
    }

    @JsonProperty(JSON_PROPERTY_USERNAME)
    public String getUsername() {
        return username;
    }

    @JsonProperty(JSON_PROPERTY_TOKEN_TYPE)
    public String getTokenType() {
        return tokenType;
    }

    @JsonProperty("iat")
    public Instant getIssuedAt() {
        return issuedAt;
    }

    @JsonProperty(JSON_PROPERTY_EXP)
    public Long getExpiresAt() {
        return expiresAt;
    }

    @JsonProperty(JSON_PROPERTY_SUBJECT)
    public String getSubject() {
        return subject;
    }

    @JsonProperty(JSON_PROPERTY_AUDIENCE)
    public String getAudience() {
        return audience;
    }

    @JsonProperty(JSON_PROPERTY_GROUP_TYPE)
    public String getGroupType() {
        return groupType;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
        calculateInitExpireAsInstant();
    }

    /**
     * @return expiration in seconds
     */
    public Instant getExpiresAtAsInstant() {
        return expiresAtInstant;
    }

    private void calculateInitExpireAsInstant() {
        this.expiresAtInstant = this.expiresAt != null ? Instant.ofEpochSecond(this.expiresAt) : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(active, audience, clientId, clientType, expiresAt, groupType, issuedAt, scope, subject, tokenType, username);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OAuth2OpaqueTokenIntrospectionResponse other = (OAuth2OpaqueTokenIntrospectionResponse) obj;
        return Objects.equals(active, other.active) && Objects.equals(audience, other.audience) && Objects.equals(clientId, other.clientId)
                && Objects.equals(clientType, other.clientType) && Objects.equals(expiresAt, other.expiresAt) && Objects.equals(groupType, other.groupType)
                && Objects.equals(issuedAt, other.issuedAt) && Objects.equals(scope, other.scope) && Objects.equals(subject, other.subject)
                && Objects.equals(tokenType, other.tokenType) && Objects.equals(username, other.username);
    }

    @Override
    public String toString() {
        return "OAuth2OpaqueTokenIntrospectionResponse [" + (active != null ? "active=" + active + ", " : "") + (scope != null ? "scope=" + scope + ", " : "")
                + (clientId != null ? "clientId=" + clientId + ", " : "") + (clientType != null ? "clientType=" + clientType + ", " : "")
                + (username != null ? "username=" + username + ", " : "") + (tokenType != null ? "tokenType=" + tokenType + ", " : "")
                + (issuedAt != null ? "issuedAt=" + issuedAt + ", " : "") + (expiresAt != null ? "expiresAt=" + expiresAt + ", " : "")
                + (expiresAtInstant != null ? "expiresAtInstant=" + expiresAtInstant + ", " : "") + (subject != null ? "subject=" + subject + ", " : "")
                + (audience != null ? "audience=" + audience + ", " : "") + (groupType != null ? "groupType=" + groupType : "") + "]";
    }

}
