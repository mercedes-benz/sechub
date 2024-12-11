// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class ZapWrapperCommandLineParser {

    private JCommander commander;

    public class ZapWrapperCommandLineParserException extends Exception {
        private static final long serialVersionUID = 1L;

        public ZapWrapperCommandLineParserException(String message, Exception e) {
            super(message, e);
        }
    }

    /**
     * Parses given command line arguments
     *
     * @param args
     * @return command line settings, never <code>null</code>
     * @throws ZapWrapperCommandLineParserException
     */
    public CommandLineSettings parse(String... args) throws ZapWrapperCommandLineParserException {
        CommandLineSettings settings = parseCommandLineParameters(args);

        if (settings.isHelpRequired()) {
            showHelp();
        }
        return settings;
    }

    private CommandLineSettings parseCommandLineParameters(String... args) throws ZapWrapperCommandLineParserException {
        CommandLineSettings settings = new CommandLineSettings();
        /* @formatter:off */
        commander = JCommander.newBuilder()
                                .programName("ZapWrapper")
                                .addObject(settings)
                                .acceptUnknownOptions(false)
                              .build();
        /* @formatter:on */
        try {
            commander.parse(args);
            return settings;
        } catch (ParameterException e) {
            throw new ZapWrapperCommandLineParserException("Parsing command line parameters failed!", e);
        }
    }

    private void showHelp() {
        commander.usage();
    }
}
