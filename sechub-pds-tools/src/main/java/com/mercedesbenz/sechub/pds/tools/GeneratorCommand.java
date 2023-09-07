// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = { CommandConstants.GENERATE }, commandDescription = "Generate files necessary for a PDS scan")
public class GeneratorCommand {

    /* @formatter:off */

    @Parameter(
            names = { "--pathToConfigFile", "-p" },
            description = "path to the sechub config file used to generate.",
            required=true
          )
    String pathToConfigFile;

    @Parameter(
            names = { "--scanType", "-s" },
            description = "scan type - must be one of: codeScan, webScan, infraScan, licenseScan, secretScan, analytics.",
            required = true
            )
    String scanType;


    @Parameter(
            names = { "--targetFolder", "-t"},
            description  = "target folder. If not defined, a temporary folder will be created"
          )
    String targetFolderPath;


    @Parameter(
            names = { "--workingDirectory", "-w"},
            description="working directory. if not set, the parent directory of the sechub configuration file will be used"
          )
    String workingDirectory;

    @Parameter(
            names = { "--createMissingFiles", "-m"},
            description="if the files defined inside the SecHub configuration file do not exist inside the working directory and this option is enabled, an empty file will be created"
            )
    boolean createMissingFiles;

    /* @formatter:on */

    public boolean isCreateMissingFiles() {
        return createMissingFiles;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public String getPathToConfigFile() {
        return pathToConfigFile;
    }

    public String getScanType() {
        return scanType;
    }

    public String getTargetFolderPath() {
        return targetFolderPath;
    }

}
