// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.mercedesbenz.sechub.pds.tools.generator.PDSSolutionTestFilesGenerator;
import com.mercedesbenz.sechub.pds.tools.handler.ConsoleHandler;
import com.mercedesbenz.sechub.pds.tools.handler.ExitHandler;
import com.mercedesbenz.sechub.pds.tools.handler.PrintStreamConsoleHandler;
import com.mercedesbenz.sechub.pds.tools.handler.SystemExitHandler;

public class PDSToolsCLI {

    ConsoleHandler consoleHandler;
    ExitHandler exitHandler;

    public PDSToolsCLI() {
        consoleHandler = new PrintStreamConsoleHandler();
        exitHandler = new SystemExitHandler();
    }

    public static void main(String[] args) throws Exception {
        PDSToolsCLI cli = new PDSToolsCLI();
        cli.start(args);
    }

    void start(String... args) throws Exception {
        GeneratorCommand generatorCommand = new GeneratorCommand();

        /* @formatter:off */
        HelpArgument helpArgument = new HelpArgument();
        JCommander jc = JCommander.newBuilder().
                console(consoleHandler).
                addCommand(generatorCommand).
                addObject(helpArgument).
                build();
        /* @formatter:on */

        try {
            jc.parse(args);
        } catch (MissingCommandException e) {
            wrongUsage("Wrong usage: " + e.getMessage(), jc);
        } catch (ParameterException e) {
            wrongUsage("Wrong usage.", jc, 3);
        }

        if (helpArgument.help) {
            jc.usage();
            exitHandler.exit(0);
        }

        String parsedCmdStr = jc.getParsedCommand();
        if (parsedCmdStr == null) {
            wrongUsage("No command defined", jc, 1);
        }
        switch (parsedCmdStr) {
        case CommandConstants.GENERATE -> generate(generatorCommand);
        default -> {
            wrongUsage("Unknown command", jc);
        }
        }
    }

    private void generate(GeneratorCommand generatorCommand) throws Exception {

        PDSSolutionTestFilesGenerator generator = new PDSSolutionTestFilesGenerator();

        generator.setConsoleHandler(consoleHandler);
        generator.generate(generatorCommand);

    }

    private void wrongUsage(String message, JCommander jc) {
        wrongUsage(message, jc, 2);
    }

    private void wrongUsage(String message, JCommander jc, int exitCode) {
        consoleHandler.error(message);
        jc.usage();
        exitHandler.exit(exitCode);
    }

}
