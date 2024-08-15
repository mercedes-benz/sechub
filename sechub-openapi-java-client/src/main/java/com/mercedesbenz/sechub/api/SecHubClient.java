// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import com.mercedesbenz.sechub.api.internal.gen.*;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubConfiguration;

import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;

/**
 * The central java API entry to access SecHub
 *
 * @author Albert Tregnaghi
 *
 */
public interface SecHubClient {

    /**
     * Adds a listener to the client. For some action on client side the listener
     * will be informed. A listener can be added only one time no matter how many
     * times this method is called.
     *
     * @param listener
     */
    void addListener(SecHubClientListener listener);

    /**
     * Removes a listener from the client (if added).
     *
     * @param listener
     */
    void removeListener(SecHubClientListener listener);

    void setUserId(String userId);

    String getUserId();

    void setApiToken(String apiToken);

    String getSealedApiToken();

    URI getServerUri();

    boolean isTrustAll();

    boolean isServerAlive() throws ApiException;

    /**
     * Uploads data as defined in given configuration
     *
     * @param projectId        the project id
     * @param jobUUID          SecHub Job UUID
     * @param configuration    SecHub Job configuration (contains information about
     *                         upload behavior (e.g. paths etc.)
     * @param workingDirectory directory where the relative paths inside
     *                         configuration model shall start from
     * @throws SecHubClientException
     */
    void userUpload(String projectId, UUID jobUUID, SecHubConfiguration configuration, Path workingDirectory) throws ApiException;

    /**
     * Downloads the full scan log for a given sechub job uuid into wanted target
     * location. This call can only be done an administrator.
     *
     * @param sechubJobUUID
     * @param downloadFilePath path to download file. If path is a folder the
     *                         filename will be
     *                         "SecHub-${sechubJobUUID}-scanlog.zip". When null, a
     *                         temp folder will be used
     * @return path to download
     */
    Path downloadFullScanLog(UUID sechubJobUUID, Path downloadFilePath) throws ApiException;

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................APIs............................ + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    ConfigurationApi atConfigurationApi();

    JobAdministrationApi atJobAdministrationApi();

    OtherApi atOtherApi();

    ProjectAdministrationApi atProjectAdministrationApi();

    SecHubExecutionApi atSecHubExecutionApi();

    SignUpApi atSignUpApi();

    SystemApi atSystemApi();

    TestingApi atTestingApi();

    UserAdministrationApi atUserAdministrationApi();

    UserProfileApi atUserProfileApi();

}
