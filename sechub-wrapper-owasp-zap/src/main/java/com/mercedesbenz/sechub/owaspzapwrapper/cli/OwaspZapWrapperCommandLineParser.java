// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;

public class OwaspZapWrapperCommandLineParser {

    private JCommander commander;

    public class OwaspZapWrapperCommandLineParserException extends Exception {
        private static final long serialVersionUID = 1L;

        public OwaspZapWrapperCommandLineParserException(String message, Exception e) {
            super(message, e);
        }
    }

    public OwaspZapScanConfiguration parse(String... args) throws OwaspZapWrapperCommandLineParserException {
        CommandLineSettings settings = parseCommandLineParameters(args);

        if (settings.isHelpRequired()) {
            showHelp();
            return null;
        }

        OwaspZapScanConfigurationFactory configFactory = new OwaspZapScanConfigurationFactory();
        return configFactory.create(settings);

    }

    private CommandLineSettings parseCommandLineParameters(String... args) throws OwaspZapWrapperCommandLineParserException {
        CommandLineSettings settings = new CommandLineSettings();
        /* @formatter:off */
        commander = JCommander.newBuilder()
                                .programName("OwaspZapWrapper")
                                .addObject(settings)
                                .acceptUnknownOptions(false)
                              .build();
        /* @formatter:on */
        try {
            commander.parse(args);
            return settings;
        } catch (ParameterException e) {
            throw new OwaspZapWrapperCommandLineParserException("Parsing command line parameters failed!", e);
        }
    }

    private void showHelp() {
        commander.usage();
    }
}
