// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.pds.commons.core.config.PDSStorageConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.storage.core.S3Setup;

@Component
public class PrepareWrapperS3PropertiesSetup implements S3Setup {

    private static final String UNDEFINED = "undefined";

    @Value("${" + PDS_STORAGE_S3_ACCESSKEY + ":" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String accessKey;

    @Value("${" + PDS_STORAGE_S3_SECRETKEY + ":" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String secretKey;

    @Value("${" + PDS_STORAGE_S3_BUCKETNAME + ":" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String bucketName;

    @Value("${" + PDS_STORAGE_S3_ENDPOINT + ":" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String endpoint;

    @Value("${" + PDS_STORAGE_S3_REGION + ":" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String region;

    /* timeout */

    @Value("${" + PDS_STORAGE_S3_TIMEOUT_CONNECTION_MILLISECONDS + ":" + S3Setup.DEFAULT_CONNECTION_TIMEOUT + "}")
    private int connectionTimeoutInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_TIMEOUT_SOCKET_MILLISECONDS + ":" + S3Setup.DEFAULT_SOCKET_TIMEOUT + "}")
    private int socketTimeoutInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_TIMEOUT_REQUEST_MILLISECONDS + ":" + S3Setup.DEFAULT_REQUEST_TIMEOUT + "}")
    private int requestTimeoutInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_TIMEOUT_EXECUTION_MILLISECONDS + ":" + S3Setup.DEFAULT_CLIENT_EXECUTION_TIMEOUT + "}")
    private int clientExecutionTimeoutInMilliseconds;

    /* connections */

    @Value("${" + PDS_STORAGE_S3_CONNECTION_MAX_POOLSIZE + ":" + S3Setup.DEFAULT_MAX_CONNECTIONS + "}")
    private int maximumAllowedConnections;

    @Value("${" + PDS_STORAGE_S3_CONNECTION_TTL_MILLISECONDS + ":" + S3Setup.DEFAULT_CONNECTION_TTL + "}")
    private long connectionTTLInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_CONNECTION_IDLE_MAX_MILLISECONDS + ":" + S3Setup.DEFAULT_CONNECTION_MAX_IDLE_MILLIS + "}")
    private long connectionMaxIdleInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_CONNECTION_IDLE_VALIDATE_MILLISECONDS + ":" + S3Setup.DEFAULT_VALIDATE_AFTER_INACTIVITY_MILLIS + "}")
    private int validateAfterInactivityInMilliseconds;

    /* signer */

    @Value("${" + PDS_STORAGE_S3_SIGNER_OVERRIDE + ":" + S3Setup.DEFAULT_SIGNER_OVERRIDE + "}")
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
