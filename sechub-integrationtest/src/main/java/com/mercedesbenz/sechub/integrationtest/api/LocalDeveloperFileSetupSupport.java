// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * A little helper class for developers: it is very inconvenient to setup some
 * standard when doing a local development in your IDE and you have to turn on
 * every junit test launch with -Dsechub.integrationtest.running=true (otherwise
 * tests will be skipped). To simplify this, this class was created. Just create
 * file `~/.sechub/sechub-developer.properties` and set there your settings. At
 * Gradle builds this will be ignored.
 *
 * @author Albert Tregnaghi
 *
 */
public class LocalDeveloperFileSetupSupport {

    public static LocalDeveloperFileSetupSupport INSTANCE = new LocalDeveloperFileSetupSupport();

    private boolean alwaysSecHubIntegrationTestRunning;

    private Set<String> importedSystemPropertyPrefixes = new LinkedHashSet<>();

    private LocalDeveloperFileSetupSupport() {

        output("Local developer support initializing");
        initAcceptedSystemProperties();

        File userHome = new File(System.getProperty("user.home"));
        File sechubHidden = new File(userHome, ".sechub");
        File sechubDevConfig = new File(sechubHidden, "sechub-developer.properties");

        if (!sechubDevConfig.exists()) {
            return;
        }
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(sechubDevConfig)) {
            properties.load(fis);
            alwaysSecHubIntegrationTestRunning = Boolean.parseBoolean(properties.getProperty(IntegrationTestSupport.SECHUB_INTEGRATIONTEST_RUNNING, "false"));
            for (Object key : properties.keySet()) {
                Object value = properties.get(key);
                if (key instanceof String && value instanceof String) {
                    String keyString = (String) key;
                    boolean importAllowed = false;
                    for (String supportedPrefix : importedSystemPropertyPrefixes) {
                        if (keyString.startsWith(supportedPrefix)) {
                            importAllowed = true;
                            break;
                        }
                    }
                    if (importAllowed) {
                        System.setProperty(keyString, (String) value);
                        output("- imported key: " + keyString + "=" + System.getProperty(keyString));
                    } else {
                        error("- ignored key : " + keyString + " (only following prefixes are accepted: " + importedSystemPropertyPrefixes + ")", null);
                    }
                }
            }

            output("Local developer support has been initialized");

        } catch (Exception e) {
            error("Was not able to load developer config file", e);
        }
    }

    private void initAcceptedSystemProperties() {
        importedSystemPropertyPrefixes.add("logging.level.");
        importedSystemPropertyPrefixes.add("org.slf4j.");
        importedSystemPropertyPrefixes.add("sechub.");
        importedSystemPropertyPrefixes.add("spring.jpa.");
        importedSystemPropertyPrefixes.add("logback.configurationFile");
    }

    public boolean isAlwaysSecHubIntegrationTestRunning() {
        return alwaysSecHubIntegrationTestRunning;
    }

    public static void main(String[] args) {
        new LocalDeveloperFileSetupSupport().toString();
    }

    private void output(String message) {
        // we do not use here sl4fj because we want to change setup before first logger
        // is used
        System.out.println(message);
    }

    private void error(String message, Throwable t) {
        // we do not use here sl4fj because we want to change setup before first logger
        // is used
        System.err.println(message);
        if (t != null) {
            t.printStackTrace();
        }
    }
}
