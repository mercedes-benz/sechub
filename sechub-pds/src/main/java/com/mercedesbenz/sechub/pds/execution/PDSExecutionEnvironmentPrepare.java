package com.mercedesbenz.sechub.pds.execution;

import static com.mercedesbenz.sechub.pds.commons.core.config.PDSStorageConstants.*;
import static com.mercedesbenz.sechub.pds.commons.core.config.PDSStorageConstantsEnvironmentVariables.*;
import static com.mercedesbenz.sechub.storage.sharevolume.spring.AbstractSharedVolumePropertiesSetup.UNDEFINED_UPLOAD_DIR;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.storage.core.S3Setup;

@Component
public class PDSExecutionEnvironmentPrepare {

    /* Shared volume properties setup for NFS */
    @Value("${" + PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR + ":" + UNDEFINED_UPLOAD_DIR + "}") // we use undefined here. Will be used in #isValid()
    private String configuredUploadDir;

    /* Shared volume properties setup for S3 */
    private static final String UNDEFINED = "undefined";

    @Value("${" + PDS_STORAGE_S3_ACCESSKEY + ":" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String accessKey;

    @Value("${" + PDS_STORAGE_S3_SECRETKEY + ":" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String secretKey;

    @Value("${" + PDS_STORAGE_S3_BUCKETNAME + ":" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String bucketName;

    @Value("${" + PDS_STORAGE_S3_ENDPOINT + ":" + UNDEFINED + "}") // we use undefined here. Will be used in isValid
    private String endpoint;

    @Value("${" + PDS_STORAGE_S3_TIMEOUT_CONNECTION_MILLISECONDS + ":" + S3Setup.DEFAULT_CONNECTION_TIMEOUT + "}")
    private int connectionTimeoutInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_TIMEOUT_SOCKET_MILLISECONDS + ":" + S3Setup.DEFAULT_SOCKET_TIMEOUT + "}")
    private int socketTimeoutInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_TIMEOUT_REQUEST_MILLISECONDS + ":" + S3Setup.DEFAULT_REQUEST_TIMEOUT + "}")
    private int requestTimeoutInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_TIMEOUT_EXECUTION_MILLISECONDS + ":" + S3Setup.DEFAULT_CLIENT_EXECUTION_TIMEOUT + "}")
    private int clientExecutionTimeoutInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_CONNECTION_MAX_POOLSIZE + ":" + S3Setup.DEFAULT_MAX_CONNECTIONS + "}")
    private int maximumAllowedConnections;

    @Value("${" + PDS_STORAGE_S3_CONNECTION_TTL_MILLISECONDS + ":" + S3Setup.DEFAULT_CONNECTION_TTL + "}")
    private long connectionTTLInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_CONNECTION_IDLE_MAX_MILLISECONDS + ":" + S3Setup.DEFAULT_CONNECTION_MAX_IDLE_MILLIS + "}")
    private long connectionMaxIdleInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_CONNECTION_IDLE_VALIDATE_MILLISECONDS + ":" + S3Setup.DEFAULT_VALIDATE_AFTER_INACTIVITY_MILLIS + "}")
    private int validateAfterInactivityInMilliseconds;

    @Value("${" + PDS_STORAGE_S3_SIGNER_OVERRIDE + ":" + S3Setup.DEFAULT_SIGNER_OVERRIDE + "}")
    private String signerOverride;

    public Map<String, String> getPDSStorageProperties() {
        Map<String, String> sharedVolumeProperties = new HashMap<>();

        sharedVolumeProperties.put(ENV_PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR, configuredUploadDir);
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_ACCESSKEY, accessKey);
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_SECRETKEY, secretKey);
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_BUCKETNAME, bucketName);
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_ENDPOINT, endpoint);
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_TIMEOUT_CONNECTION_MILLISECONDS, String.valueOf(connectionTimeoutInMilliseconds));
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_TIMEOUT_SOCKET_MILLISECONDS, String.valueOf(socketTimeoutInMilliseconds));
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_TIMEOUT_REQUEST_MILLISECONDS, String.valueOf(requestTimeoutInMilliseconds));
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_TIMEOUT_EXECUTION_MILLISECONDS, String.valueOf(clientExecutionTimeoutInMilliseconds));
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_CONNECTION_MAX_POOLSIZE, String.valueOf(maximumAllowedConnections));
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_CONNECTION_TTL_MILLISECONDS, String.valueOf(connectionTTLInMilliseconds));
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_CONNECTION_IDLE_MAX_MILLISECONDS, String.valueOf(connectionMaxIdleInMilliseconds));
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_CONNECTION_IDLE_VALIDATE_MILLISECONDS, String.valueOf(validateAfterInactivityInMilliseconds));
        sharedVolumeProperties.put(ENV_PDS_STORAGE_S3_SIGNER_OVERRIDE, signerOverride);

        return sharedVolumeProperties;
    }
}
