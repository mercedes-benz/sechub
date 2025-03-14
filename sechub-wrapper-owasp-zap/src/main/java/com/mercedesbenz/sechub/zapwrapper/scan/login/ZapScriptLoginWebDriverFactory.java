// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.io.File;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.zapwrapper.config.ProxyInformation;

public class ZapScriptLoginWebDriverFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScriptLoginWebDriverFactory.class);

    private static final Dimension DEFAULT_WEBDRIVER_RESOLUTION = new Dimension(1920, 1080);

    public FirefoxDriver createFirefoxWebdriver(ProxyInformation proxyInformation, File pacFilePath, boolean headless) {

        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            LOG.info("Using firefox in headless mode.");
            options.addArguments("-headless");
        }
        options.addArguments("-private");

        if (proxyInformation != null) {
            LOG.info("Adding proxy to firefox browser options.");
            // TODO 2024-11-25 jan: add additional proxy data like credentials
            String proxyString = "%s:%s".formatted(proxyInformation.getHost(), proxyInformation.getPort());
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyString);
            proxy.setSslProxy(proxyString);
            options.setProxy(proxy);
        } else if (pacFilePath != null) {
            String pacFilePathUrlAsString = "file://" + pacFilePath.getAbsolutePath();
            LOG.info("Adding PAC file: {} to firefox browser profile and add profile to firefox options.", pacFilePathUrlAsString);
            FirefoxProfile profile = new FirefoxProfile();
            // see: http://kb.mozillazine.org/Network.proxy.type
            profile.setPreference("network.proxy.type", 2);
            // see: http://kb.mozillazine.org/Network.proxy.autoconfig_url
            profile.setPreference("network.proxy.autoconfig_url", pacFilePathUrlAsString);

            options.setProfile(profile);
        }
        LOG.info("Creating selenium firefox driver.");
        FirefoxDriver firefox = new FirefoxDriver(options);
        // Set the window size, some application need a windows size to render correctly
        // even in headless mode
        firefox.manage().window().setSize(DEFAULT_WEBDRIVER_RESOLUTION);
        return firefox;
    }
}
