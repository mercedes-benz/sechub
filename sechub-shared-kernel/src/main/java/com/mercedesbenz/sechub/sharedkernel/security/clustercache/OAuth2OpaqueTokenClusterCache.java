// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security.clustercache;

import static java.util.Objects.*;

import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = OAuth2OpaqueTokenClusterCache.TABLE_NAME)
public class OAuth2OpaqueTokenClusterCache {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "IDENTITY_OAUTH2_OPAQUETOKEN_CACHE";

    public static final String COLUMN_OPAQUE_TOKEN = "OPAQUE_TOKEN";

    public static final String COLUMN_INTROSPECTION_RESPONSE = "INTROSPECTION_RESPONSE";

    public static final String COLUMN_CREATED_AT = "CREATED_AT";

    public static final String COLUMN_DURATION = "DURATION";

    public static final String COLUMN_EXPIRES_AT = "EXPIRES_AT";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = "OAuth2OpaqueTokenClusterCache";

    public static final String PROPERTY_CREATED_AT = "createdAt";
    public static final String PROPERTY_DURATION = "duration";
    public static final String PROPERTY_EXPIRES_AT = "expiresAt";

    @Id
    @Column(name = COLUMN_OPAQUE_TOKEN, unique = true, nullable = false)
    String opaqueToken;

    @Column(name = COLUMN_INTROSPECTION_RESPONSE, nullable = false)
    String introSpectionResponse;

    @Column(name = COLUMN_CREATED_AT, nullable = false)
    Instant createdAt;

    @Column(name = COLUMN_DURATION, nullable = false)
    Duration duration;

    @Column(name = COLUMN_EXPIRES_AT, nullable = false)
    Instant expiresAt; // this field is only to have simpler queries

    @Version
    @Column(name = "VERSION")
    Integer version;

    /* JPA only */
    @SuppressWarnings("unused")
    private OAuth2OpaqueTokenClusterCache() {

    }

    public OAuth2OpaqueTokenClusterCache(String opaqueToken, String introSpectionResponse, Duration duration, Instant createdAt) {
        this.opaqueToken = opaqueToken;
        update(introSpectionResponse, createdAt, duration);
    }

    void update(String introSpectionResponse, Instant createdAt, Duration duration) {
        this.introSpectionResponse = introSpectionResponse;
        this.createdAt = requireNonNull(createdAt, "Parameter 'createdAt' must not be null");
        this.duration = requireNonNull(duration, "Parameter 'duration' must not be null");
        this.expiresAt = createdAt.plus(duration);
    }

    public String getOpaqueToken() {
        return opaqueToken;
    }

    public String getIntroSpectionResponse() {
        return introSpectionResponse;
    }

    public Duration getDuration() {
        return duration;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((opaqueToken == null) ? 0 : opaqueToken.hashCode());
        return result;
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
        OAuth2OpaqueTokenClusterCache other = (OAuth2OpaqueTokenClusterCache) obj;
        if (opaqueToken == null) {
            if (other.opaqueToken != null)
                return false;
        } else if (!opaqueToken.equals(other.opaqueToken)) {
            return false;
        }
        return true;
    }

}
