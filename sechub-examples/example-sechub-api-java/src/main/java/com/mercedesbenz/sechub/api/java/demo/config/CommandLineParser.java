// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class CommandLineParser {

    private JCommander commander;

    /**
     * Parses given arguments
     *
     * @param args
     * @return configuration or <code>null</code> when only help wanted
     * @throws SecHubClientConfigurationRuntimeException
     */
    public CommandLineSettings parse(String... args) {
        CommandLineSettings settings = parseCommandLineParameters(args);
        if (settings.isHelpRequired()) {
            showHelp();
            return null;
        }
        return settings;
    }
    

    private CommandLineSettings parseCommandLineParameters(String... args) {
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
            throw new IllegalStateException("Parsing command line parameters failed!", e);
        }
    }

    private void showHelp() {
        commander.usage();
    }

}
