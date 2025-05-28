// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.container.keycloak;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mercedesbenz.sechub.developertools.container.AbstractTestContainer;
import com.mercedesbenz.sechub.developertools.container.BashScriptContainerLaunchConfig;

public class KeycloakTestContainer extends AbstractTestContainer {

    public KeycloakTestContainer(int testPort, String user, String password) {
        super(testPort, user, password);
    }

    @Override
    public void start() throws Exception {
        Path scriptPath = pathUtils.resolveScriptpath(KeycloakTestContainer.class, "keycloak/start.sh");

        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Does not exist:" + scriptPath);
        }
        config = new BashScriptContainerLaunchConfig(scriptPath);
        config.getEnvironment().put("KEYCLOAK_ADMIN", username);
        config.getEnvironment().put("KEYCLOAK_ADMIN_PASSWORD", password);
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
}
