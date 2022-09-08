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

        case "--help":
            showHelp();
            exitHandler.exit(0);
        case "--generate":
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
        consoleHandler.output("Usage:");
        consoleHandler.output("--help                                          ");
        consoleHandler.output("   show this output");
        consoleHandler.output("");
        consoleHandler.output("--generate ${secHubConfigFilePath} ${scanType} [$targetFolderPath] ");
        consoleHandler.output("   generate PDS test files for given config. Given scan type");
        consoleHandler.output("   can be: codeScan, licenseScan, webScan etc. When no target folder");
        consoleHandler.output("   is defined, a temp folder will be created and used");
    }
}
