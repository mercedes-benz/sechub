package com.daimler.sechub.integrationtest.api;

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
        File userHome = new File(System.getProperty("user.home"));
        File sechubHidden = new File(userHome, ".sechub");
        File sechubDevConfig = new File(sechubHidden, "sechub-developer.properties");

        String buildGradleEnv = System.getenv("SECHUB_BUILD_GRADLE");
        if (Boolean.parseBoolean(buildGradleEnv)) {
            LOG.info("Recognized gradle build, skip check for :{}", sechubDevConfig);
            return;
        }

        if (!sechubDevConfig.exists()) {
            return;
        }
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(sechubDevConfig)) {
            properties.load(fis);
            alwaysSecHubIntegrationTestRunning = Boolean.parseBoolean(properties.getProperty(IntegrationTestSetup.SECHUB_INTEGRATIONTEST_RUNNING, "false"));
        } catch (Exception e) {
            LOG.error("Was not able to load developer config file", e);
        }
    }

    public boolean isAlwaysSecHubIntegrationTestRunning() {
        return alwaysSecHubIntegrationTestRunning;
    }
}
