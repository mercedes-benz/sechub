// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A little helper class for developers: it is very inconvenient to setup some
 * standard when doing a local development in your IDE and you have to turn on
 * every junit test launch with -Dsechub.integrationtest.running=true (otherwise
 * tests will be skipped). To simplify this, this class was created. Just create
 * file `~/.sechub/sechub-developer.properties` and set there your settings. At
 * Gradle builds this will be ignored. well then!
 *
 * @author Albert Tregnaghi
 *
 */
public class LocalDeveloperFileSetupSupport {

    public static LocalDeveloperFileSetupSupport INSTANCE = new LocalDeveloperFileSetupSupport();

    private static final Logger LOG = LoggerFactory.getLogger(LocalDeveloperFileSetupSupport.class);

    private boolean alwaysSecHubIntegrationTestRunning;

    private LocalDeveloperFileSetupSupport() {

        logInfo("Local developer support initializing");
        File userHome = new File(System.getProperty("user.home"));
        File sechubHidden = new File(userHome, ".sechub");
        File sechubDevConfig = new File(sechubHidden, "sechub-developer.properties");

        String buildGradleEnv = System.getProperty("sechub.build.gradle");
        if (Boolean.parseBoolean(buildGradleEnv)) {
            logInfo("Recognized gradle build, skip check for :" + sechubDevConfig.getAbsolutePath());
            return;
        }

        if (!sechubDevConfig.exists()) {
            return;
        }
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(sechubDevConfig)) {
            properties.load(fis);
            alwaysSecHubIntegrationTestRunning = Boolean.parseBoolean(properties.getProperty(IntegrationTestSetup.SECHUB_INTEGRATIONTEST_RUNNING, "false"));
            logInfo("Local developer support has been initialized");
            Object logbackConfigurationFile = properties.get("logback.configurationFile");
            if (logbackConfigurationFile instanceof String) {
                System.getProperty("logback.configurationFile", logbackConfigurationFile.toString());
            }

        } catch (Exception e) {
            logError("Was not able to load developer config file", e);
        }
    }

    public boolean isAlwaysSecHubIntegrationTestRunning() {
        return alwaysSecHubIntegrationTestRunning;
    }

    public static void main(String[] args) {
        new LocalDeveloperFileSetupSupport().toString();
    }

    private void logInfo(String message) {
        if (LOG == null) {
            // as some unclear reasons this can happen in IDEs when executing junit tests -
            // so fallback necessary
            System.out.println("NO_LOG (info):" + message);
            return;
        }
        LOG.info(message);
    }

    private void logError(String message, Throwable t) {
        if (LOG == null) {
            // as some unclear reasons this can happen in IDEs when executing junit tests -
            // so fallback necessarys
            System.err.println("NO_LOG (error):" + message);
            t.printStackTrace();
            return;
        }
        LOG.error(message, t);
    }
}
