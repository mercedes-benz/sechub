// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.container.keycloak;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.container.AbstractTestContainer;
import com.mercedesbenz.sechub.developertools.container.AbstractTestContainerStarter;

public class LocalTestKeycloakStarter extends AbstractTestContainerStarter {

    private static final String DEFAULT_FIXED_PORT = "8080";

    public LocalTestKeycloakStarter() {
        super(LoggerFactory.getLogger(LocalTestKeycloakStarter.class));
    }

    /**
     * Starts a local keycloak test container.
     *
     * @param args optional parts in following order:
     *
     *             <pre>
     * ${port} ${admin} ${password} ${clientSecret}
     *             </pre>
     *
     *             When port is not set, the test container will start with port
     *             8080 {@value #DEFAULT_FIXED_PORT}. When no admin user is set the
     *             user will be auto generated. A defined user name must have at
     *             least 8 characters. A password must be at last 12 characters
     *             long. If not defined a strong password will be generated. When no
     *             client secret is set, a secret will be auto generated.
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String testPort = args.length > 0 ? args[0] : DEFAULT_FIXED_PORT;
        String admin = args.length > 1 ? args[1] : UUID.randomUUID().toString();
        String password = args.length > 2 ? args[2] : UUID.randomUUID().toString();
        String clientSecret = args.length > 3 ? args[3] : UUID.randomUUID().toString();

        assertLength(admin, "Keycloak admin user name", 8);
        assertLength(password, "Keycloak admin password", 12);
        assertLength(clientSecret, "Keycloak client-secret", 12);

        int keycloakPort = Integer.parseInt(testPort);

        new LocalTestKeycloakStarter().start(keycloakPort, admin, password, clientSecret);
    }

    @Override
    protected void createInfoFile(AbstractTestContainer container) throws IOException {
        if (!(container instanceof KeycloakTestContainer)) {
            throw new IllegalArgumentException("Container must be of type KeyCloakTestContainer, but was:" + container.getClass().getName());
        }

        Path created = Files.createFile(filePath);

        StringBuilder sb = new StringBuilder();
        sb.append("KC_BOOTSTRAP_ADMIN_USERNAME").append(container.getUsername());
        sb.append("\nKC_BOOTSTRAP_ADMIN_PASSWORD").append(container.getPassword());
        sb.append("\nSECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET").append(((KeycloakTestContainer) container).getClientSecret());

        Files.write(created, sb.toString().getBytes());
    }

    @Override
    protected KeycloakTestContainer createContainer(int port, String username, String password, String clientSecret) {
        return new KeycloakTestContainer(port, username, password, clientSecret);
    }

    @Override
    protected String getInfoFileName(int port) {
        return "keycloak_container_" + port + ".info";
    }
}
