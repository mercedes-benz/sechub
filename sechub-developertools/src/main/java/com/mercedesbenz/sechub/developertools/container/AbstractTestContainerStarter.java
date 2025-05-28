// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.container;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

public abstract class AbstractTestContainerStarter {

    private ContainerPathUtils containerPathUtils;

    protected Path filePath;
    protected AbstractTestContainer container;
    protected Logger LOG;

    public AbstractTestContainerStarter(Logger LOG) {
        this.LOG = LOG;
        this.containerPathUtils = new ContainerPathUtils();
    }

    protected abstract AbstractTestContainer createContainer(int port, String username, String password, String dbName);

    protected abstract void createInfoFile(AbstractTestContainer container) throws IOException;

    protected abstract String getInfoFileName(int port);

    public static void assertLength(String toInspect, String hint, int minLength) {
        if (toInspect.length() < minLength) {
            throw new IllegalArgumentException(hint + ". Min length:" + minLength + ", found:" + toInspect.length());
        }
    }

    public void start(int port, String username, String password, String dbName) throws Exception {
        Runtime.getRuntime().addShutdownHook(createShutdownHookThread());

        container = createContainer(port, username, password, dbName);
        initializeDirectoryAndFiles(port);

        LOG.info("Starting container on port: {}", port);
        container.start();

        createInfoFile(container);

        Thread inspectThread = new Thread(new InspectStillWantedToRun(), "Inspect still wanted to run");
        inspectThread.start();
    }

    private void initializeDirectoryAndFiles(int port) throws Exception {
        String fileName = getInfoFileName(port);

        File folder = containerPathUtils.resolvePackageBuildTmpFolder(AbstractTestContainerStarter.class);
        filePath = new File(folder, fileName).toPath();
        dropOldInfoFileAndWaitIfNecessary();
    }

    private void dropOldInfoFileAndWaitIfNecessary() throws IOException, InterruptedException {
        if (Files.exists(filePath)) {
            LOG.warn("Found existing container info file:{}, will delete old file and wait for other container shutdown!", filePath);
            Files.delete(filePath);
            LOG.warn("Deleted file:{}, waiting for other container shutdown!", filePath);
            Thread.sleep(4000);
        }
    }

    private Thread createShutdownHookThread() {
        return new Thread(new InspectShutdown(), "Inspect shutdown");
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class InspectShutdown implements Runnable {
        @Override
        public void run() {
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
