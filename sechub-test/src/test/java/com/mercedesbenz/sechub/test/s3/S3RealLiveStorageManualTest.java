// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test.s3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.sharedkernel.storage.MultiStorageService;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.S3Setup;
import com.mercedesbenz.sechub.storage.core.SharedVolumeSetup;
import com.mercedesbenz.sechub.test.ManualTest;

/**
 * This is not really a test, but a simple test program where we can check if
 * the implementation is working in real live world as well. This is important
 * when having S3 hosting not at amazon, but by another provider. So you can
 * check if the amazon client API does really work with your environment. <br>
 * <br>
 * We have an automated test {@link AwsS3JobStorageTest} which uses s3mock to
 * simulate S3 buckets. So this main class should normally be not necessary -
 * but is just an ensurance it really works in production environment as
 * expected. <br>
 * <br>
 * You just have to provide your S3 credentials as environment variables. Look
 * at output failures - missing entries are explained there.
 *
 * @author Albert Tregnaghi
 *
 */
class S3RealLiveStorageManualTest implements ManualTest {

    private static final String S3_OBJECT_NAME = "testdata";

    @Test
    void manualTestByDeveloper() throws Exception {

        /* setup */
        SharedVolumeSetup setup = createFakeSharedVolumeNotValid();
        S3Setup s3Setup = createS3SetupByEnvironmentVariables();

        MultiStorageService service = new MultiStorageService(setup, s3Setup);

        UUID jobUUID = UUID.randomUUID();
        JobStorage jobStorage = service.getJobStorage("test-only", jobUUID);

        /* check preconditions */
        boolean existsBefore = jobStorage.isExisting(S3_OBJECT_NAME);
        String testDataAsString = "This is some test data as a simple string\nJust another line...";

        /* store */
        jobStorage.store(S3_OBJECT_NAME, new StringInputStream(testDataAsString), testDataAsString.getBytes().length);
        boolean existsAfterStore = jobStorage.isExisting(S3_OBJECT_NAME);

        /* fetch */
        InputStream inputStream = jobStorage.fetch(S3_OBJECT_NAME);
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(reader);
        String result = br.readLine();
        br.close();

        /* delete all */
        jobStorage.deleteAll();

        /* check delete done */
        boolean existsAfterDelete = jobStorage.isExisting(S3_OBJECT_NAME);

        System.out.println("exists before storage:" + existsBefore);
        System.out.println("exists after storage:" + existsAfterStore);
        System.out.println("fetched string from object store:" + result);
        System.out.println("exists after delete:" + existsAfterDelete);

        if (existsBefore) {
            System.err.println("existed before!");
            System.exit(1);
        }
        if (!existsAfterStore) {
            System.err.println("was not stored!");
            System.exit(1);
        }
        if (!testDataAsString.equals(result)) {
            System.err.println("result was not as expected:" + result);
            System.exit(1);
        }
        if (existsAfterDelete) {
            System.err.println("data was not as expected:" + result);
            System.exit(1);
        }
    }

    private static S3Setup createS3SetupByEnvironmentVariables() {
        String bucketURL = System.getenv("S3_ENDPOINT");
        if (bucketURL == null || bucketURL.isEmpty()) {
            throw new IllegalStateException("S3_ENDPOINT missing");
        }
        String bucketAccessKey = System.getenv("S3_ACCESSKEY");
        if (bucketAccessKey == null || bucketAccessKey.isEmpty()) {
            throw new IllegalStateException("S3_ACCESSKEY missing");
        }
        String bucketSecretKey = System.getenv("S3_SECRETKEY");
        if (bucketSecretKey == null || bucketSecretKey.isEmpty()) {
            throw new IllegalStateException("S3_SECRETKEY missing");
        }
        String bucketName = System.getenv("S3_BUCKET_NAME");
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalStateException("S3_BUCKET_NAME missing");
        }

        S3Setup s3Setup = new S3Setup() {

            @Override
            public String getAccessKey() {
                return bucketAccessKey;
            }

            @Override
            public String getSecretkey() {
                return bucketSecretKey;
            }

            @Override
            public String getEndPoint() {
                return bucketURL;
            }

            @Override
            public String getBucketName() {
                return bucketName;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public int getConnectionTimeoutInMilliseconds() {
                return S3Setup.DEFAULT_CONNECTION_TIMEOUT;
            }

            @Override
            public int getSocketTimeoutInMilliseconds() {
                return S3Setup.DEFAULT_SOCKET_TIMEOUT;
            }

            @Override
            public int getRequestTimeOutInMilliseconds() {
                return S3Setup.DEFAULT_REQUEST_TIMEOUT;
            }

            @Override
            public int getClientExecutionTimeoutInMilliseconds() {
                return S3Setup.DEFAULT_CLIENT_EXECUTION_TIMEOUT;
            }

            @Override
            public int getMaximumAllowedConnections() {
                return S3Setup.DEFAULT_MAX_CONNECTIONS;
            }

            @Override
            public long getConnectionTTLinMilliseconds() {
                return S3Setup.DEFAULT_CONNECTION_TTL;
            }

            @Override
            public long getConnectionMaxIdleInMilliseconds() {
                return S3Setup.DEFAULT_CONNECTION_MAX_IDLE_MILLIS;
            }

            @Override
            public int getValidateAfterInactivityInMilliseconds() {
                return S3Setup.DEFAULT_VALIDATE_AFTER_INACTIVITY_MILLIS;
            }

            @Override
            public String getSignerOverride() {
                return S3Setup.DEFAULT_SIGNER_OVERRIDE;
            }

        };
        return s3Setup;
    }

    private static SharedVolumeSetup createFakeSharedVolumeNotValid() {
        /* just a fake shared volume setup */
        SharedVolumeSetup setup = new SharedVolumeSetup() {

            @Override
            public boolean isAvailable() {
                return false;
            }

            @Override
            public String getUploadDir() {
                return null;
            }
        };
        return setup;
    }
}
