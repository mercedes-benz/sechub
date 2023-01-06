// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContextFactory;

public class OwaspZapWrapperCommandLineParser {

    private JCommander commander;

    public class OwaspZapWrapperCommandLineParserException extends Exception {
        private static final long serialVersionUID = 1L;

        public OwaspZapWrapperCommandLineParserException(String message, Exception e) {
            super(message, e);
        }
    }

    /**
     * Parses given arguments
     *
     * @param args
     * @return configuration or <code>null</code> when only help wanted
     * @throws OwaspZapWrapperCommandLineParserException
     */
    public OwaspZapScanContext parse(String... args) throws OwaspZapWrapperCommandLineParserException {
        CommandLineSettings settings = parseCommandLineParameters(args);

        if (settings.isHelpRequired()) {
            showHelp();
            return null;
        }

        OwaspZapScanContextFactory configFactory = new OwaspZapScanContextFactory();
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
