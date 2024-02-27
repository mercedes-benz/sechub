// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = { CommandConstants.SYSTEMTEST }, commandDescription = "Execute system test(s)")
public class SystemTestCommand {

    /* @formatter:off */

    @Parameter(
            names = { "--file", "-f" },
            description = "Path to system test configuration file (json)",
            required=true
          )
    String pathToConfigFile;

    @Parameter(
            names = { "--dry", "-d" },
            description = "When enabled, we use 'dry run', means no network communication to SecHub or PDS is done",
            required=false
          )
    private boolean dryRun;

    @Parameter(
            names = { "--remote", "-r" },
            description = "Switch to remote run",
            required = false
            )
    private  boolean remoteRun;


    @Parameter(
            names = { "--workspace-rootfolder", "-w"},
            description  = "workspace root folder. If not efined, a temporary folder will be created",
            required=false
          )
    String workspaceFolder;


    @Parameter(
            names = { "--additional-resources-folder", "-a"},
            description="addtional resources folder. If not set, the current directory is used as fallback",
            required=false
          )
    String additionalResourcesFolder;

    @Parameter(
            names = { "--sechub-solution-rootfolder", "-s"},
            description="SecHub solution root folder. If not defined, the location is automatically calculated by using the pds solution root folder location",
            required=false
          )
    String secHubSolutionRootFolder;

    @Parameter(
            names = { "--pds-solutions-rootfolder", "-p"},
            description="PDS solutions root folder. If not defined a temp folder will be created as a fallback. But this works only, when no real PDS solutions shall be started inside the tests",
            required=false
            )
    String pdsSolutionsRootFolder;


    @Parameter(
            names = { "--run-tests", "-rt" },
            description = "The (comma separated) name(s) of the tests to run. When defined, only those tests are run. When nothing defined, all system tests are executed.",
            required=false
          )
    List<String> testsToRun = new ArrayList<>();

    /* @formatter:on */

    public String getPdsSolutionsRootFolder() {
        return pdsSolutionsRootFolder;
    }

    public String getSecHubSolutionRootFolder() {
        return secHubSolutionRootFolder;
    }

    public String getAdditionalResourcesFolder() {
        return additionalResourcesFolder;
    }

    public String getPathToConfigFile() {
        return pathToConfigFile;
    }

    public String getWorkspaceFolder() {
        return workspaceFolder;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isRemoteRun() {
        return remoteRun;
    }

    public List<String> getTestsToRun() {
        return testsToRun;
    }

}
