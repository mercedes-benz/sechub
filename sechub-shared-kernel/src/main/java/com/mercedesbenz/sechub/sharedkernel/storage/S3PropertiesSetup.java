// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;
import com.mercedesbenz.sechub.storage.core.S3Setup;

@Component
public class S3PropertiesSetup implements S3Setup {

    private static final String UNDEFINED = "undefined";
    @MustBeDocumented(value = "Defines the access key for used S3 bucket", scope = DocumentationScopeConstants.SCOPE_STORAGE, secret = true)
    @Value("${sechub.storage.s3.accesskey:" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String accessKey;

    @MustBeDocumented(value = "Defines the secret key for used S3 bucket", scope = DocumentationScopeConstants.SCOPE_STORAGE, secret = true)
    @Value("${sechub.storage.s3.secretkey:" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String secretKey;

    @MustBeDocumented(value = "Defines the S3 bucket name", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.bucketname:" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String bucketName;

    @MustBeDocumented(value = "Defines the S3 endpoint - e.g. https://play.min.io", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.endpoint:" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String endpoint;

    @MustBeDocumented(value = "S3 client region. Supported are offical AWS region names and additionally: `default` and `current`. When"
            + " `current` is used, the implementation will try to resolve the current region automatically.", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.region:" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String region;

    /* timeout */

    @MustBeDocumented(value = "S3 client timeout (in milliseconds) for creating new connections.", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.timeout.connection.milliseconds:" + S3Setup.DEFAULT_CONNECTION_TIMEOUT + "}")
    private int connectionTimeoutInMilliseconds;

    @MustBeDocumented(value = "S3 client timeout (in milliseconds) for reading from a connected socket.", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.timeout.socket.milliseconds:" + S3Setup.DEFAULT_SOCKET_TIMEOUT + "}")
    private int socketTimeoutInMilliseconds;

    @MustBeDocumented(value = "S3 client timeout (in milliseconds) for a request. 0 means it is disabled.", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.timeout.request.milliseconds:" + S3Setup.DEFAULT_REQUEST_TIMEOUT + "}")
    private int requestTimeoutInMilliseconds;

    @MustBeDocumented(value = "S3 client timeout (in milliseconds) for execution. 0 means it is disabled.", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.timeout.execution.milliseconds:" + S3Setup.DEFAULT_CLIENT_EXECUTION_TIMEOUT + "}")
    private int clientExecutionTimeoutInMilliseconds;

    /* connections */

    @MustBeDocumented(value = "S3 client max connection pool size.", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.connection.max.poolsize:" + S3Setup.DEFAULT_MAX_CONNECTIONS + "}")
    private int maximumAllowedConnections;

    @MustBeDocumented(value = "S3 client expiration time (in milliseconds) for a connection in the connection pool. -1 means deactivated", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.connection.ttl.milliseconds:" + S3Setup.DEFAULT_CONNECTION_TTL + "}")
    private long connectionTTLInMilliseconds;

    @MustBeDocumented(value = "S3 client maximum idle time (in milliseconds) for a connection in the connection pool.", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.connection.idle.max.milliseconds:" + S3Setup.DEFAULT_CONNECTION_MAX_IDLE_MILLIS + "}")
    private long connectionMaxIdleInMilliseconds;

    @MustBeDocumented(value = "S3 client time (in milliseconds) a connection can be idle in the connection pool before it must be validated that it's still open.", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.connection.idle.validate.milliseconds:" + S3Setup.DEFAULT_VALIDATE_AFTER_INACTIVITY_MILLIS + "}")
    private int validateAfterInactivityInMilliseconds;

    /* signer */

    @MustBeDocumented(value = "Can be used to override the default name of the signature algorithm used to sign requests.", scope = DocumentationScopeConstants.SCOPE_STORAGE)
    @Value("${sechub.storage.s3.signer.override:" + S3Setup.DEFAULT_SIGNER_OVERRIDE + "}")
    private String signerOverride;

    @Override
    public String getAccessKey() {
        return accessKey;
    }

    @Override
    public String getSecretkey() {
        return secretKey;
    }

    @Override
    public String getEndPoint() {
        return endpoint;
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

    @Override
    public boolean isAvailable() {
        boolean inValid = false;

        inValid = inValid || UNDEFINED.equals(accessKey);
        inValid = inValid || UNDEFINED.equals(secretKey);
        inValid = inValid || UNDEFINED.equals(endpoint);
        inValid = inValid || UNDEFINED.equals(bucketName);
        inValid = inValid || UNDEFINED.equals(region);

        return !inValid;
    }

    @Override
    public int getConnectionTimeoutInMilliseconds() {
        return connectionTimeoutInMilliseconds;
    }

    @Override
    public int getSocketTimeoutInMilliseconds() {
        return socketTimeoutInMilliseconds;
    }

    @Override
    public int getRequestTimeOutInMilliseconds() {
        return requestTimeoutInMilliseconds;
    }

    @Override
    public int getClientExecutionTimeoutInMilliseconds() {
        return clientExecutionTimeoutInMilliseconds;
    }

    @Override
    public int getMaximumAllowedConnections() {
        return maximumAllowedConnections;
    }

    @Override
    public long getConnectionTTLinMilliseconds() {
        return connectionTTLInMilliseconds;
    }

    @Override
    public long getConnectionMaxIdleInMilliseconds() {
        return connectionMaxIdleInMilliseconds;
    }

    @Override
    public int getValidateAfterInactivityInMilliseconds() {
        return validateAfterInactivityInMilliseconds;
    }

    @Override
    public String getSignerOverride() {
        return signerOverride;
    }

    @Override
    public String getRegion() {
        return region;
    }

}
