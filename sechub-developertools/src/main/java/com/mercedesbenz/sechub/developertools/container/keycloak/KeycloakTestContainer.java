// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.container.keycloak;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mercedesbenz.sechub.developertools.container.AbstractTestContainer;
import com.mercedesbenz.sechub.developertools.container.BashScriptContainerLaunchConfig;

public class KeycloakTestContainer extends AbstractTestContainer {

    private final String clientSecret;

    public KeycloakTestContainer(int testPort, String user, String password, String clientSecret) {
        super(testPort, user, password);
        this.clientSecret = clientSecret;
    }

    @Override
    public void start() throws Exception {
        Path scriptPath = pathUtils.resolveScriptpath(KeycloakTestContainer.class, "keycloak/start.sh");

        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Does not exist:" + scriptPath);
        }
        config = new BashScriptContainerLaunchConfig(scriptPath);
        config.getEnvironment().put("KC_BOOTSTRAP_ADMIN_USERNAME", username);
        config.getEnvironment().put("KC_BOOTSTRAP_ADMIN_PASSWORD", password);
        config.getEnvironment().put("SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET", clientSecret);
        config.getParameters().add(String.valueOf(port));

        launcher.start(config);
    }

    @Override
    public void stop() throws IOException {
        Path scriptPath = pathUtils.resolveScriptpath(KeycloakTestContainer.class, "keycloak/stop.sh");

        config = new BashScriptContainerLaunchConfig(scriptPath);
        config.getParameters().add(String.valueOf(getPort()));
        launcher.start(config);
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
