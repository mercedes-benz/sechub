package com.mercedesbenz.sechub.developertools.container.keycloak;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalTestKeycloakStarter {

    private static final String DEFAULT_FIXED_PORT = "8080";
    private static final Logger LOG = LoggerFactory.getLogger(LocalTestKeycloakStarter.class);

    private Path filePath;
    private KeycloakTestContainer container;

    public static void main(String[] args) throws Exception {
        String testPort = args.length > 0 ? args[0] : DEFAULT_FIXED_PORT;
        String admin = args.length > 1 ? args[1] : UUID.randomUUID().toString();
        String password = args.length > 2 ? args[2] : UUID.randomUUID().toString();

        int keycloakPort = Integer.parseInt(testPort);

        new LocalTestKeycloakStarter().start(keycloakPort, admin, password);
    }

    private void start(int port, String admin, String password) throws Exception {
        Runtime.getRuntime().addShutdownHook(createShutdownHookThread());

        container = new KeycloakTestContainer(port, admin, password);
        initializeDirectoryAndFiles(port);

        LOG.info("start keycloak local on port:{}", port);
        container.start();

        createInfoFile(container);

        Thread inspectThread = new Thread(new InspectStillWantedToRun(), "Inspect still wanted to run");
        // inspectThread.setDaemon(true);
        inspectThread.start();
    }

    private Thread createShutdownHookThread() {
        return new Thread(new InspectShutdown(), "Inspect shutdown");
    }

    private void createInfoFile(KeycloakTestContainer container) throws IOException {
        Path created = Files.createFile(filePath);

        StringBuilder sb = new StringBuilder();
        sb.append("keycloak admin user:").append(container.getAdmin()).append("\n");
        sb.append("keycloak admin password:").append(container.getPassword()).append("\n");

        Files.write(created, sb.toString().getBytes());
    }

    private void initializeDirectoryAndFiles(int port) throws Exception {
        String fileName = "keycloak_container_" + port + ".info";
        File folder = new File("build/tmp");
        if (!folder.exists()) {
            Files.createDirectories(folder.toPath());
        }
        filePath = new File(folder, fileName).toPath();
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

            try {
                container.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
