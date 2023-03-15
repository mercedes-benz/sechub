// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import java.io.File;

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
        if (args.length == 0) {
            showHelpAndExit("Arguments missing", 1);
        }
        String command = args[0];
        switch (command) {

        case PDSToolsCLiConstants.CMD_HELP:
            showHelp();
            exitHandler.exit(0);
        case PDSToolsCLiConstants.CMD_GENERATE:
            PDSSolutionTestFilesGenerator generator = new PDSSolutionTestFilesGenerator();
            generator.setOutputHandler(consoleHandler);

            if (args.length < 3 || args.length > 4) {
                showHelpAndExit("Generate command needs 2 additional parameters: 1. config file path, 2.scan type", 3);
            }
            String pathToConfigFile = args[1];
            String scanType = args[2];
            File targetFolder = null;
            if (args.length == 4) {
                String targetFolderPath = args[3];
                targetFolder = new File(targetFolderPath);
                if (!targetFolder.exists()) {
                    targetFolder.mkdirs();
                }
            }
            generator.generate(pathToConfigFile, scanType, targetFolder);

            break;

        default:
            showHelpAndExit("Unrecognized command:" + command, 2);
        }
    }

    private void showHelpAndExit(String message, int exitCode) {
        consoleHandler.output("Wrong usage:" + message);
        showHelp();
        exitHandler.exit(exitCode);
    }

    private void showHelp() {
        consoleHandler.output("SecHub PDS tools CLI");
        consoleHandler.output("--------------------");
        consoleHandler.output("Usage:");

        for (PDSToolCLICommand cmd : MainPDSToolsCLICommands.values()) {
            StringBuilder cmdSb = new StringBuilder();
            cmdSb.append(cmd.getCommandString());
            for (PDSToolCLICommandArgument argument : cmd.getArguments()) {
                cmdSb.append(" ");
                if (argument.isOptional()) {
                    cmdSb.append("[");
                }
                cmdSb.append("${");
                cmdSb.append(argument.getName());
                cmdSb.append("}");

                if (argument.isOptional()) {
                    cmdSb.append("]");
                }
            }

            consoleHandler.output(cmdSb.toString());
            consoleHandler.output("   " + cmd.getDescription());

            for (PDSToolCLICommandArgument argument : cmd.getArguments()) {
                StringBuilder argSb = new StringBuilder();
                argSb.append("     - ");
                argSb.append(argument.getName());

                if (argument.isOptional()) {
                    argSb.append(" (optional)");
                }
                consoleHandler.output(argSb.toString());
                consoleHandler.output("        " + argument.getDescription());
            }

        }
    }
}
