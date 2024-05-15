// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.storage.core.S3Setup;

@Component
public class PrepareWrapperS3PropertiesSetup implements S3Setup {

    private static final String UNDEFINED = "undefined";
    @PDSMustBeDocumented(value = "Defines the access key for used s3 bucket", scope = "storage", secret = true)
    @Value("${pds.storage.s3.accesskey:" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String accessKey;

    @PDSMustBeDocumented(value = "Defines the secret key for used s3 bucket", scope = "storage", secret = true)
    @Value("${pds.storage.s3.secretkey:" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String secretKey;

    @PDSMustBeDocumented(value = "Defines the s3 bucket name", scope = "storage")
    @Value("${pds.storage.s3.bucketname:" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String bucketName;

    @PDSMustBeDocumented(value = "Defines the s3 endpoint - e.g. https://play.min.io", scope = "storage")
    @Value("${pds.storage.s3.endpoint:" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String endpoint;

    /* timeout */

    @PDSMustBeDocumented(value = "S3 client timeout (in milliseconds) for creating new connections.", scope = "storage")
    @Value("${pds.storage.s3.timeout.connection.milliseconds:" + S3Setup.DEFAULT_CONNECTION_TIMEOUT + "}")
    private int connectionTimeoutInMilliseconds;

    @PDSMustBeDocumented(value = "S3 client timeout (in milliseconds) for reading from a connected socket.", scope = "storage")
    @Value("${pds.storage.s3.timeout.socket.milliseconds:" + S3Setup.DEFAULT_SOCKET_TIMEOUT + "}")
    private int socketTimeoutInMilliseconds;

    @PDSMustBeDocumented(value = "S3 client timeout (in milliseconds) for a request. 0 means it is disabled.", scope = "storage")
    @Value("${pds.storage.s3.timeout.request.milliseconds:" + S3Setup.DEFAULT_REQUEST_TIMEOUT + "}")
    private int requestTimeoutInMilliseconds;

    @PDSMustBeDocumented(value = "S3 client timeout (in milliseconds) for execution. 0 means it is disabled.", scope = "storage")
    @Value("${pds.storage.s3.timeout.execution.milliseconds:" + S3Setup.DEFAULT_CLIENT_EXECUTION_TIMEOUT + "}")
    private int clientExecutionTimeoutInMilliseconds;

    /* connections */

    @PDSMustBeDocumented(value = "S3 client max connection pool size.", scope = "storage")
    @Value("${pds.storage.s3.connection.max.poolsize:" + S3Setup.DEFAULT_MAX_CONNECTIONS + "}")
    private int maximumAllowedConnections;

    @PDSMustBeDocumented(value = "S3 client expiration time (in milliseconds) for a connection in the connection pool. -1 means deactivated", scope = "storage")
    @Value("${pds.storage.s3.connection.ttl.milliseconds:" + S3Setup.DEFAULT_CONNECTION_TTL + "}")
    private long connectionTTLInMilliseconds;

    @PDSMustBeDocumented(value = "S3 client maximum idle time (in milliseconds) for a connection in the connection pool.", scope = "storage")
    @Value("${pds.storage.s3.connection.idle.max.milliseconds:" + S3Setup.DEFAULT_CONNECTION_MAX_IDLE_MILLIS + "}")
    private long connectionMaxIdleInMilliseconds;

    @PDSMustBeDocumented(value = "S3 client time (in milliseconds) a connection can be idle in the connection pool before it must be validated that it's still open.", scope = "storage")
    @Value("${pds.storage.s3.connection.idle.validate.milliseconds:" + S3Setup.DEFAULT_VALIDATE_AFTER_INACTIVITY_MILLIS + "}")
    private int validateAfterInactivityInMilliseconds;

    /* signer */

    @PDSMustBeDocumented(value = "Can be used to override the default name of the signature algorithm used to sign requests.", scope = "storage")
    @Value("${pds.storage.s3.signer.override:" + S3Setup.DEFAULT_SIGNER_OVERRIDE + "}")
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

}
