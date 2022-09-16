// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.mercedesbenz.sechub.api.java.SecHubClient;
import com.mercedesbenz.sechub.api.java.demo.cli.exceptions.SecHubClientConfigurationRuntimeException;

public class CommandLineParser {

    private JCommander commander;

    /**
     * Parses given arguments
     *
     * @param args
     * @return configuration or <code>null</code> when only help wanted
     * @throws SecHubClientConfigurationRuntimeException
     */
    public SecHubClient parse(String... args) throws SecHubClientConfigurationRuntimeException {
        CommandLineSettings settings = parseCommandLineParameters(args);
        if (settings.isHelpRequired()) {
            showHelp();
            return null;
        }
        SecHubClientConfigurationFactory configFactory = new SecHubClientConfigurationFactory();
        return configFactory.create(settings);
    }

    private CommandLineSettings parseCommandLineParameters(String... args) throws SecHubClientConfigurationRuntimeException {
        CommandLineSettings settings = new CommandLineSettings();
        /* @formatter:off */
        commander = JCommander.newBuilder()
                                .programName("OpenAPITestTool")
                                .addObject(settings)
                                .acceptUnknownOptions(false)
                              .build();
        /* @formatter:on */
        try {
            commander.parse(args);
            return settings;
        } catch (ParameterException e) {
            throw new SecHubClientConfigurationRuntimeException("Parsing command line parameters failed!", e);
        }
    }

    private void showHelp() {
        commander.usage();
    }

}
