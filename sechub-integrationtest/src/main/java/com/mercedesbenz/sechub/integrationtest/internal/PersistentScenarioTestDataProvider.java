// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides persistent test data for integration tests - e.g. growing ids for
 * test scenarios.
 *
 * @author Albert Tregnaghi
 *
 */
public class PersistentScenarioTestDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentScenarioTestDataProvider.class);

    private static final String BASE_SECHUB_INTEGRATIONTEST_DATA_KEY = "sechub.integrationtest.data.";
    private static final String SECHUB_INTEGRATIONTEST_DATA_GROWINGID = BASE_SECHUB_INTEGRATIONTEST_DATA_KEY + "growingid";
    private int grow;
    File file;
    private Properties properties;

    public PersistentScenarioTestDataProvider(GrowingScenario scenario) {
        this(new File(IntegrationTestFileSupport.getTestfileSupport().getIntegrationTestDataFolder(),
                "scenario-testdata_" + scenario.getClass().getSimpleName().toLowerCase() + ".properties"));
    }

    /**
     * Only for PersistentScenarioTestDataProviderTest and internal call from other
     * constructor. Do not use directly.
     *
     * @param file
     */
    PersistentScenarioTestDataProvider(File file) {
        if (file == null) {
            throw new IllegalStateException("Wrong usage: file may not be null!");
        }
        this.file = file;
        ensurePropertyFileExists();

    }

    private void ensurePropertyFileExists() {
        LOG.trace("Ensure test scenario property file exists: {}", file);

        properties = new Properties();
        if (file.exists()) {
            LOG.trace("File exists: {}", file);

            boolean loaded = false;
            int tryCount = 0;
            Exception lastException = null;
            while (!loaded && tryCount < 5) {
                tryCount++;

                LOG.trace("Start load of properties file: {} per stream. Try count:{}", file, tryCount);
                try (FileInputStream fis = new FileInputStream(file)) {
                    properties.load(fis);
                    LOG.trace("Properties loaded: {}, contains: {}", file.getName(), properties);
                    String d = properties.getProperty(SECHUB_INTEGRATIONTEST_DATA_GROWINGID);
                    LOG.trace("Properties: {}, growing id value: {}", file.getName(), d);
                    if (d == null) {
                        grow = 0;
                    } else {
                        grow = Integer.parseInt(d);
                    }
                    LOG.trace("Properties: {}, grow set to: {}", file.getName(), grow);
                    loaded = true;
                } catch (Exception e) {
                    lastException = e;
                    LOG.trace("Properties load failed, will wait shortly and retry some time", e);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (!loaded) {
                LOG.trace("Properties load failed, will no longer retry, but delete file: {}", file);

                this.file.delete();
                throw new IllegalStateException("Cannot read growid file: " + file.getAbsolutePath() + ", so deleted as fallback", lastException);
            }
        }
        if (!file.exists()) {
            file.getParentFile().mkdirs();

            LOG.trace("File NOT exists: {}", file);
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("cannot create growid file:" + file.getAbsolutePath(), e);
            }
        }
    }

    /**
     * Increases grow id and stores updated data automatically to file system
     *
     * @throws IllegalStateException when grow id becomes greater than 9999 because
     *                               names would become to big for generated user
     *                               names etc.
     */
    public void increaseGrowId() {
        grow++;
        if (grow > 9999) {
            throw new IllegalStateException(
                    "Grow ID >9999 - not valid. This should only happen on local development.\nHow to handle this? Please call `gradlew cleanIntegrationTestData` to have a clean counter again and restart tests.");
        }
        properties.put(SECHUB_INTEGRATIONTEST_DATA_GROWINGID, "" + grow);
        store();

    }

    private void store() {
        LOG.trace("Try to store property file: {}", file);

        File parentFolder = file.getParentFile();
        if (!parentFolder.exists()) {
            if (!parentFolder.mkdirs()) {
                throw new IllegalStateException("Was not able to create parent folder: " + parentFolder.getAbsolutePath());
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "DO NOT CHANGE THIS FILE!");
        } catch (IOException e) {
            throw new IllegalStateException("cannot store: " + file.getAbsolutePath(), e);
        }
        LOG.trace("Stored property file: {}, content was: {}", file.getName(), properties);
    }

    public String getGrowId() {
        StringBuilder sb = new StringBuilder();
        sb.append(grow);
        while (sb.length() < 4) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

}
