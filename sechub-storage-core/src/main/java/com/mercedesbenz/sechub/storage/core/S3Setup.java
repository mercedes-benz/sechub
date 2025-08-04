// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.core;

public interface S3Setup extends StorageSetup {

    /** S3 client default timeout for creating new connections. */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 10 * 1000;

    /** S3 client default timeout for reading from a connected socket. */
    public static final int DEFAULT_SOCKET_TIMEOUT = 50 * 1000;

    /**
     * S3 client default timeout for a request. This is disabled by default.
     */
    public static final int DEFAULT_REQUEST_TIMEOUT = 0;

    /**
     * S3 client default timeout for a request. This is disabled by default.
     */
    public static final int DEFAULT_CLIENT_EXECUTION_TIMEOUT = 0;

    /** S3 client default max connection pool size. */
    public static final int DEFAULT_MAX_CONNECTIONS = 50;

    /**
     * S3 client default expiration time (in milliseconds) for a connection in the
     * connection pool.
     */
    public static final long DEFAULT_CONNECTION_TTL = -1;

    /**
     * S3 client default maximum idle time (in milliseconds) for a connection in the
     * connection pool.
     */
    public static final long DEFAULT_CONNECTION_MAX_IDLE_MILLIS = 60 * 1000;

    /**
     * S3 client default time a connection can be idle in the connection pool before
     * it must be validated that it's still open.
     */
    public static final int DEFAULT_VALIDATE_AFTER_INACTIVITY_MILLIS = 5 * 1000;

    public static final String DEFAULT_SIGNER_OVERRIDE = "AWSS3V4SignerType";

    String getAccessKey();

    String getSecretkey();

    String getEndPoint();

    String getBucketName();

    /* time out setup */
    int getConnectionTimeoutInMilliseconds();

    int getSocketTimeoutInMilliseconds();

    int getRequestTimeOutInMilliseconds();

    int getClientExecutionTimeoutInMilliseconds();

    /* connections */
    int getMaximumAllowedConnections();

    long getConnectionTTLinMilliseconds();

    long getConnectionMaxIdleInMilliseconds();

    int getValidateAfterInactivityInMilliseconds();

    /* signer */
    String getSignerOverride();

    String getRegion();

}