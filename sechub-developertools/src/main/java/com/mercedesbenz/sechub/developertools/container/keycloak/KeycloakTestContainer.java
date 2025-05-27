// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.container.keycloak;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mercedesbenz.sechub.developertools.container.BashScriptContainerLaunchConfig;
import com.mercedesbenz.sechub.developertools.container.BashScriptContainerLauncher;

public class KeycloakTestContainer {

    private final int testPort;
    private final String admin;
    private final String password;
    private BashScriptContainerLaunchConfig config;
    private final BashScriptContainerLauncher launcher;

    public KeycloakTestContainer(int testPort, String admin, String password) {
        this.testPort = testPort;
        this.admin = admin;
        this.password = password;
        this.launcher = new BashScriptContainerLauncher();
    }

    private static Path resolveScript(String scriptName) {
        // Resolve the script path relative to the class location
        // It should not matter where the TestContainer is executed from
        String classLocation = KeycloakTestContainer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        Path classPath = Paths.get(classLocation).getParent();

        return classPath.resolve(Paths.get("../../../scripts/container/keycloak", scriptName)).normalize();
    }

    public void start() throws Exception {
        Path scriptPath = resolveScript("start.sh");
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Does not exist:" + scriptPath);
        }
        config = new BashScriptContainerLaunchConfig(scriptPath);
        config.getEnvironment().put("KEYCLOAK_ADMIN", admin);
        config.getEnvironment().put("KEYCLOAK_ADMIN_PASSWORD", password);
        config.getParameters().add(String.valueOf(testPort));
        launcher.start(config);
    }

    public void stop() throws IOException {
        Path scriptPath = resolveScript("stop.sh");
        config = new BashScriptContainerLaunchConfig(scriptPath);
        config.getParameters().add(String.valueOf(testPort));
        launcher.start(config);
    }

    public String getAdmin() {
        return admin;
    }

    public String getPassword() {
        return password;
    }
}
