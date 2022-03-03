// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.scan.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.login.BasicLoginConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.SessionManagementType;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.OwaspZapApiResponseHelper;

public class HTTPBasicAuthScan extends AbstractAuthScan {

    private BasicLoginConfiguration basicLoginConfiguration;

    private static final Logger LOG = LoggerFactory.getLogger(HTTPBasicAuthScan.class);

    public HTTPBasicAuthScan(ClientApi clientApi, OwaspZapScanConfiguration scanConfig) {
        super(clientApi, scanConfig);
    }

    @Override
    public void init() throws ClientApiException {
        this.basicLoginConfiguration = this.scanConfig.getSecHubWebScanConfiguration().getLogin().get().getBasic().get();
        initAuthenticationMethod();
        initScanUser();

    }

    private void initAuthenticationMethod() throws ClientApiException {
        String realm = "";
        if (basicLoginConfiguration.getRealm().isPresent()) {
            realm = basicLoginConfiguration.getRealm().get();
        }
        String port = Integer.toString(scanConfig.getTargetUri().getPort());
        /* @formatter:off */
		StringBuilder authmethodconfigparams = new StringBuilder();
		authmethodconfigparams.append("hostname=").append(urlEncodeUTF8(scanConfig.getTargetUri().getHost()))
							  .append("&realm=").append(urlEncodeUTF8(realm))
							  .append("&port=").append(urlEncodeUTF8(port));
		/* @formatter:on */
        LOG.info("For scan {}: Setting authentication.", scanConfig.getContextName());
        String authmethodname = scanConfig.getAuthenticationType().getOwaspZapAuthenticationMethod();
        clientApi.authentication.setAuthenticationMethod(contextId, authmethodname, authmethodconfigparams.toString());

        String methodName = SessionManagementType.HTTP_AUTH_SESSION_MANAGEMENT.getOwaspZapSessionManagementMethod();
        clientApi.sessionManagement.setSessionManagementMethod(contextId, methodName, null);
    }

    private void initScanUser() throws ClientApiException {
        username = new String(basicLoginConfiguration.getUser());
        String password = new String(basicLoginConfiguration.getPassword());

        ApiResponse creatUserResponse = clientApi.users.newUser(contextId, username);
        userId = OwaspZapApiResponseHelper.getIdOfApiRepsonse(creatUserResponse);

        /* @formatter:off */
		StringBuilder authCredentialsConfigParams = new StringBuilder();
		authCredentialsConfigParams.append("username=").append(urlEncodeUTF8(username))
								   .append("&password=").append(urlEncodeUTF8(password));
		/* @formatter:on */

        LOG.info("For scan {}: Setting up user.", scanConfig.getContextName());
        clientApi.users.setAuthenticationCredentials(contextId, userId, authCredentialsConfigParams.toString());
        String enabled = "true";
        clientApi.users.setUserEnabled(contextId, userId, enabled);

        clientApi.forcedUser.setForcedUser(contextId, userId);
        clientApi.forcedUser.setForcedUserModeEnabled(true);
    }
}
