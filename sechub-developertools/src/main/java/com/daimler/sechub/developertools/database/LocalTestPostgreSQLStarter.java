package com.daimler.sechub.developertools.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
 * The default export port is 49152. User and Password are "test"
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

    private static final Logger LOG = LoggerFactory.getLogger(LocalTestPostgreSQLStarter.class);
    private Path filePath;

    /**
     * Starts a local postgres test container
     * 
     * @param args optional parts in following order: <port> <username> <password>
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String testPort = "49152";
        String testUserName = null;
        String testPassword = null;
        if (args.length > 0) {
            testPort = args[0];
        }
        if (args.length > 1) {
            testUserName = args[1];
        }
        if (args.length > 2) {
            testPassword = args[2];
        }
        int postgresContainerPort = Integer.parseInt(testPort);
        new LocalTestPostgreSQLStarter().start(postgresContainerPort, testUserName, testPassword);
    }

    private void start(int port, String testUserName, String testPassword) throws Exception {
        Runtime.getRuntime().addShutdownHook(createShutdownHookThread());

        try (PostgreSQLTestContainer container = new PostgreSQLTestContainer(port, testUserName, testPassword)) {
            initializeDirectoryAndFiles(port);

            LOG.info("start postgres local on port:{}", port);
            container.start();

            createInfoFile(container);

            Thread inspectThread = new Thread(new InspectStillWantedToRun(), "Inspect still wanted to run");
            inspectThread.start();
        }

    }

    private Thread createShutdownHookThread() {
        return new Thread(new InspectShutdown(), "Inspect shutdown");
    }

    private void createInfoFile(PostgreSQLTestContainer container) throws IOException {
        Path created = Files.createFile(filePath);

        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/bash\n");
        sb.append("\nTEST_DB_URL=" + container.getJdbcUrl());
        sb.append("\nTEST_DB_USERNAME=" + container.getUsername());
        sb.append("\nTEST_DB_PASSWORD" + container.getPassword());

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
            LOG.warn("Deleted file:{}, wait now for other container shutdown!", filePath);
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
