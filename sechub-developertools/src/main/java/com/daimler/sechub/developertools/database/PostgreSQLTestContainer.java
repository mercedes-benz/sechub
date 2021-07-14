// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.database;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLTestContainer extends PostgreSQLContainer<PostgreSQLTestContainer> {

    private int testPort;
    private String testUserName;
    private String testPassword;
    private String testDatabaseName;

    /**
     * Creates a postgres test container with wanted exposed port
     * 
     * @param exposedPostgresPort port to be exposed. use -1 when you want a random
     *                            one
     * @param imageVersion2 
     */
    public PostgreSQLTestContainer(int exposedPostgresPort, String userName, String password, String testDatabaseName, String imageVersion) {
        super(imageVersion);
        this.testPort = exposedPostgresPort;
        this.testUserName = userName;
        this.testDatabaseName=testDatabaseName;
        this.testPassword = password;
    }

    @Override
    protected void configure() {
        if (testDatabaseName!=null) {
            withDatabaseName(testDatabaseName);
        }
        if (testPort != -1) {
            addFixedExposedPort(testPort, POSTGRESQL_PORT);
        }
        if (testUserName != null) {
            withUsername(testUserName);
        }
        if (testPassword != null) {
            withPassword(testPassword);
        }
        super.configure();
    }

    @Override
    public void stop() {
        // do nothing, JVM handles shut down
    }
}