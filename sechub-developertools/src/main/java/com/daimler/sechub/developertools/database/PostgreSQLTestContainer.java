package com.daimler.sechub.developertools.database;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLTestContainer extends PostgreSQLContainer<PostgreSQLTestContainer> {
    
    private static final String IMAGE_VERSION = "postgres:11.1";
    private int testPort;
    private String testUserName;
    private String testPassword;

    /**
     * Creates a postgres test container with wanted exposed port
     * @param exposedPostgresPort port to be exposed. use -1 when you want a random one
     */
    public PostgreSQLTestContainer(int exposedPostgresPort, String userName, String password) {
        super(IMAGE_VERSION);
        this.testPort=exposedPostgresPort;
        this.testUserName=userName;
        this.testPassword=password;
    }


    @Override
    protected void configure() {
        super.configure();
        if(testPort!=-1) {
            addFixedExposedPort(testPort, POSTGRESQL_PORT);
        }
        if (testUserName!=null) {
            withUsername(testUserName);
        }
        if (testPassword!=null) {
            withPassword(testPassword);
        }
    }

    @Override
    public void stop() {
        // do nothing, JVM handles shut down
    }
}