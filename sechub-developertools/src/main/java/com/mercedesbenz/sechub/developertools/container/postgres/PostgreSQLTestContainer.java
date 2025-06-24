// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.container.postgres;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mercedesbenz.sechub.developertools.container.AbstractTestContainer;
import com.mercedesbenz.sechub.developertools.container.BashScriptContainerLaunchConfig;

public class PostgreSQLTestContainer extends AbstractTestContainer {

    private final String databaseName;

    /**
     * Creates a postgres test container with wanted exposed port
     *
     * @param exposedPostgresPort port to be exposed. use -1 when you want a random
     *                            one
     * @param userName
     * @param password
     * @param databaseName
     */
    public PostgreSQLTestContainer(int exposedPostgresPort, String userName, String password, String databaseName) {
        super(exposedPostgresPort, userName, password);
        this.databaseName = databaseName;
    }

    @Override
    public void start() throws IOException {
        Path scriptPath = pathUtils.resolveScriptpath(PostgreSQLTestContainer.class, "postgres/start.sh");
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Does not exist:" + scriptPath);
        }

        config = new BashScriptContainerLaunchConfig(scriptPath);
        config.getEnvironment().put("POSTGRES_DB_USER", username);
        config.getEnvironment().put("POSTGRES_DB_PASSWORD", password);
        config.getEnvironment().put("POSTGRES_DB_NAME", databaseName);
        config.getParameters().add("" + port);

        launcher.start(config);
    }

    @Override
    public void stop() throws IOException {
        Path path = pathUtils.resolveScriptpath(PostgreSQLTestContainer.class, "postgres/stop.sh");
        config = new BashScriptContainerLaunchConfig(path);

        config.getParameters().add("" + port);

        launcher.start(config);
    }

    public String getJdbcUrl() {
        return "jdbc:postgresql://127.0.0.1:" + port + "/" + databaseName + "?loggerLevel=OFF";
    }
}