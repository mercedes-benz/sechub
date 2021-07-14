// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This is a special starter mechanism for a PostgreSQL test container.
 * <h2>Why not as a JUnit4 rule or Junit5 extension?</h2> Normally test
 * containers are used as a rule (Junit4) or an extension (Junit5). But here we
 * want only to startup the test container to have a running PostgreSQL database
 * without any installation effort. <br>
 * This can be used for <b>local development</b> but also to <b>run integration
 * test servers</b> with a PostgreSQL instance instead of h2.<br>
 * <br>
 * 
 * The default export port is {@value #DEFAULT_FIXED_PORT}. User and Password
 * are "test"
 * <h2>Howtos</h2>
 * <h3>Howto stop the running test container in integration tests starter
 * skripts when I have started in a separated process ?</h3> <br>
 * Just delete the created file "./build/tmp/postgres_container_${port}.info"-
 * This will be inspected and leads to automated shutdown. If the JVM does shut
 * down and the file still exists, it will be automatically removed.
 * 
 * 
 * @author Albert Tregnaghi
 *
 */
public class LocalTestPostgreSQLStarter {

    private static final String DEFAULt_POSTGRES_IMAGE = "postgres:11.12";
    private static final String DEFAULT_FIXED_PORT = "49152";

    private static final Logger LOG = LoggerFactory.getLogger(LocalTestPostgreSQLStarter.class);
    private Path filePath;

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
     *             be auto generated. A defined user name must have at least 12
     *             characters. A password must be at last 12 characters long. If not
     *             defined a strong password will be generated. The docker image
     *             used for postgres container is per default
     *             {@value #DEFAULt_POSTGRES_IMAGE}e
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String testPort = DEFAULT_FIXED_PORT;
        String testUserName = null;
        String testPassword = null;
        String testDatabaseName = null;
        String imageVersion = DEFAULt_POSTGRES_IMAGE;

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
        if (args.length > 4) {
            imageVersion = args[4];
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

        assertLength(testUserName, "Username has not min length", 12);
        assertLength(testPassword, "Password has not min length", 20);

        int postgresContainerPort = Integer.parseInt(testPort);
        
        new LocalTestPostgreSQLStarter().start(postgresContainerPort, testUserName, testPassword,testDatabaseName, imageVersion);
    }

    private static void assertLength(String toInspect, String hint, int minLength) {
        if (toInspect.length() < 12) {
            throw new IllegalArgumentException(hint + ". Min length:" + minLength + ", found:" + toInspect.length());
        }

    }

    private void start(int port, String testUserName, String testPassword, String testDatabaseName, String imageVersion) throws Exception {
        Runtime.getRuntime().addShutdownHook(createShutdownHookThread());

        StringBuilder sb = new StringBuilder();
        sb.append("\n*************************************************");
        sb.append("\n******** IMPORTANT SECURITY NOTES ***************");
        sb.append("\n*************************************************\n");
        sb.append("\nBe aware that this starts a testcontainer with 0.0.0.0 as IP-Adress!");
        sb.append("\nThis means that everybody inside your network can see this instance as well.");
        sb.append("\nIf the testcontainer docker image has vulnerabilities you will have those exposed!");
        sb.append("\nMitigations:");
        sb.append("\n- define a dedicated firewall and deny external access");
        sb.append("\n- define your docker defaults to apply 127.0.0.1 instead of 0.0.0.0 per default or");
        sb.append("\n- at least use strong passwords!\n");
        sb.append("\n*************************************************");
        sb.append("\n******** END OF SECURITY NOTES ******************");
        sb.append("\n*************************************************");
        
        LOG.warn("Warn user about potential security risks"+sb.toString());
        
        try (PostgreSQLTestContainer container = new PostgreSQLTestContainer(port, testUserName, testPassword,testDatabaseName, imageVersion)) {
            initializeDirectoryAndFiles(port);

            LOG.info("start postgres local on port:{}", port);
            container.start();

            createInfoFile(container);

            Thread inspectThread = new Thread(new InspectStillWantedToRun(), "Inspect still wanted to run");
            inspectThread.start();
        }
        LOG.warn("Warn user about potential security risks"+sb.toString());

    }

    private Thread createShutdownHookThread() {
        return new Thread(new InspectShutdown(), "Inspect shutdown");
    }

    private void createInfoFile(PostgreSQLTestContainer container) throws IOException {
        Path created = Files.createFile(filePath);

        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/bash\n");
        sb.append("\nPOSTGRES_DB_URL=" + container.getJdbcUrl());
        sb.append("\nPOSTGRES_DB_USERNAME=" + container.getUsername());
        sb.append("\nPOSTGRES_DB_PASSWORD=" + container.getPassword());

        Files.write(created, sb.toString().getBytes());
    }

    private void initializeDirectoryAndFiles(int port) throws Exception {
        String fileName = "postgres_container_" + port + ".info";
        File folder = new File("build/tmp");
        if (!folder.exists()) {
            Files.createDirectories(folder.toPath());
        }
        File file = new File(folder, fileName);
        filePath = file.toPath();

        dropOldInfoFileAndWaitIfNecessary();
    }

    private void dropOldInfoFileAndWaitIfNecessary() throws IOException, InterruptedException {
        if (Files.exists(filePath)) {
            LOG.warn("Found existing container info file:{}, will delte old file and wait for other container shutdown!", filePath);
            // other instance running
            Files.delete(filePath);
            LOG.warn("Deleted file:{}, waiting for other container shutdown!", filePath);
            // we wait some seconds to give other instance the possibility to shutdown
            Thread.sleep(4000);
        }
    }

    private class InspectStillWantedToRun implements Runnable {

        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (Files.exists(filePath));

        }

    }

    private class InspectShutdown implements Runnable {

        @Override
        public void run() {
            if (filePath != null) {
                try {
                    // cleanup info file
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

}
