// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.container.postgres;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.container.AbstractTestContainer;
import com.mercedesbenz.sechub.developertools.container.AbstractTestContainerStarter;

public class LocalTestPostgreSQLStarter extends AbstractTestContainerStarter {

    private static final String DEFAULT_FIXED_PORT = "49152";

    public LocalTestPostgreSQLStarter() {
        super(LoggerFactory.getLogger(LocalTestPostgreSQLStarter.class));
    }

    /**
     * Starts a local postgres test container. <b>`test`</b>
     *
     * @param args optional parts in following order:
     *
     *             <pre>
     *  ${port} ${username} ${password} ${dbName} ${imageName}
     *             </pre>
     *
     *             When port is -1, the test container will start with a random port
     *             number. When no parameter is defined port will be
     *             {@value #DEFAULT_FIXED_PORT}. When no user is set the user will
     *             be auto generated. A defined user name must have at least 8
     *             characters. A password must be at last 12 characters long. If not
     *             defined a strong password will be generated. The psql version is
     *             the default postgres image from alpine linux base image defined
     *             inside
     *             sechub-developertools/scripts/container/postgres/Dockerfile
     *             {@value #DEFAULt_POSTGRES_IMAGE}e
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String testPort = DEFAULT_FIXED_PORT;
        String testUserName = null;
        String testPassword = null;
        String testDatabaseName = null;

        if (args.length > 0) {
            testPort = args[0];
        }
        if (args.length > 1) {
            testUserName = args[1];
        }
        if (args.length > 2) {
            testPassword = args[2];
        }
        if (args.length > 3) {
            testDatabaseName = args[3];
        }

        if (testUserName == null) {
            testUserName = UUID.randomUUID().toString();
        }

        if (testPassword == null) {
            testPassword = UUID.randomUUID().toString();
        }
        if (testDatabaseName == null) {
            testDatabaseName = "test";
        }

        assertLength(testUserName, "Username has not min length", 8);
        assertLength(testPassword, "Password has not min length", 20);

        int postgresContainerPort = Integer.parseInt(testPort);

        new LocalTestPostgreSQLStarter().start(postgresContainerPort, testUserName, testPassword, testDatabaseName);
    }

    @Override
    protected void createInfoFile(AbstractTestContainer container) throws IOException {
        if (!(container instanceof PostgreSQLTestContainer)) {
            throw new IllegalArgumentException("Container must be of type PostgreSQLTestContainer, but was:" + container.getClass().getName());
        }

        Path created = Files.createFile(filePath);

        StringBuilder sb = new StringBuilder();
        sb.append("POSTGRES_DB_URL=").append(((PostgreSQLTestContainer) container).getJdbcUrl());
        sb.append("\nPOSTGRES_DB_USERNAME=").append(container.getUsername());
        sb.append("\nPOSTGRES_DB_PASSWORD=").append(container.getPassword());

        Files.write(created, sb.toString().getBytes());
    }

    @Override
    protected PostgreSQLTestContainer createContainer(int port, String username, String password, String dbName) {
        return new PostgreSQLTestContainer(port, username, password, dbName);
    }

    @Override
    protected String getInfoFileName(int port) {
        return "postgres_container_" + port + ".info";
    }
}
