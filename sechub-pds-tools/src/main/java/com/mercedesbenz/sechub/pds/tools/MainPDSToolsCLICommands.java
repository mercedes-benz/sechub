// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.ScanType;

public enum MainPDSToolsCLICommands implements PDSToolCLICommand {

    /* @formatter:off */
    HELP(PDSToolsCLiConstants.CMD_HELP, "Show this help output."),

    GENERATE(PDSToolsCLiConstants.CMD_GENERATE,"Generate PDS test files.",

            new PDSToolCLICommandArgument("secHubConfigFilePath","path to the sechub config file used to generate."),
            new PDSToolCLICommandArgument("scanType","scan type - must be one of: "+generateScanTypeString()+"."),
            new PDSToolCLICommandArgument("targetFolderPath","When not defined, a temp folder will be created and used.",true)

            ),
    /* @formatter:on */

    ;

    private static String generateScanTypeString() {
        StringBuilder sb = new StringBuilder();

        ScanType type;
        for (Iterator<ScanType> it = PDSToolsCLiConstants.NO_REPORT_OR_UNKNOWN.iterator(); it.hasNext();) {
            type = it.next();
            sb.append(type.getId());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private String description;
    private List<PDSToolCLICommandArgument> arguments;
    private String commandString;

    private MainPDSToolsCLICommands(String commandString, String description, PDSToolCLICommandArgument... arguments) {
        this.description = description;
        this.commandString = commandString;
        if (arguments == null || arguments.length == 0) {
            this.arguments = new ArrayList<>();
        } else {
            this.arguments = Arrays.asList(arguments);
        }
    }

    public String getCommandString() {
        return commandString;
    }

    public List<PDSToolCLICommandArgument> getArguments() {
        return arguments;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
