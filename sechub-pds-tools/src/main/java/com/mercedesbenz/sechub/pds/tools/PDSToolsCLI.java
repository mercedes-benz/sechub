// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import java.io.IOException;
import java.util.Set;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.mercedesbenz.sechub.pds.tools.generator.PDSSolutionTestFilesGenerator;
import com.mercedesbenz.sechub.pds.tools.handler.ConsoleHandler;
import com.mercedesbenz.sechub.pds.tools.handler.ExitHandler;
import com.mercedesbenz.sechub.pds.tools.handler.PrintStreamConsoleHandler;
import com.mercedesbenz.sechub.pds.tools.handler.SystemExitHandler;
import com.mercedesbenz.sechub.pds.tools.systemtest.SystemTestLauncher;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRunResult;

public class PDSToolsCLI {

    ConsoleHandler consoleHandler;
    ExitHandler exitHandler;
    SystemTestLauncher systemTestLauncher;

    public PDSToolsCLI() {
        consoleHandler = new PrintStreamConsoleHandler();
        exitHandler = new SystemExitHandler();
        systemTestLauncher = new SystemTestLauncher();
    }

    public static void main(String[] args) throws Exception {
        PDSToolsCLI cli = new PDSToolsCLI();
        cli.start(args);
    }

    void start(String... args) throws Exception {
        GeneratorCommand generatorCommand = new GeneratorCommand();
        SystemTestCommand systemTestCommand = new SystemTestCommand();

        /* @formatter:off */
        HelpArgument helpArgument = new HelpArgument();
        JCommander jc = JCommander.newBuilder().
                console(consoleHandler).
                addCommand(generatorCommand).
                addCommand(systemTestCommand).
                addObject(helpArgument).
                build();
        /* @formatter:on */

        try {
            jc.parse(args);
        } catch (MissingCommandException e) {
            wrongUsage("Wrong usage: " + e.getMessage(), jc);
        } catch (ParameterException e) {
            wrongUsage("Wrong usage: " + e.getMessage(), jc, 3);
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
        case CommandConstants.SYSTEMTEST -> systemtest(systemTestCommand);
        default -> {
            wrongUsage("Unknown command", jc);
        }
        }
    }

    private void systemtest(SystemTestCommand systemTestCommand) {
        try {
            SystemTestResult result = systemTestLauncher.launch(systemTestCommand);
            if (result == null) {
                consoleHandler.error("No system test result available");
                exitHandler.exit(2);
            }

            if (result.hasProblems()) {
                consoleHandler.error("Problems detected:");
                for (String problem : result.getProblems()) {
                    consoleHandler.error("- " + problem);
                }
            }

            if (result.hasFailedTests()) {
                Set<SystemTestRunResult> runs = result.getRuns();
                for (SystemTestRunResult run : runs) {
                    if (run.hasFailed()) {
                        String message = "Test '" + run.getTestName() + "' FAILED!\n" + run.getFailure().getMessage() + "\n" + run.getFailure().getDetails();
                        consoleHandler.error(message);
                    }
                }
            }

            if (result.hasFailedTests() || result.hasProblems()) {
                exitHandler.exit(1);
            }

        } catch (IOException e) {
            consoleHandler.error("Was not able to launch system test: " + e.getMessage());
            exitHandler.exit(1);
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
