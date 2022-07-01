package com.mercedesbenz.sechub.wrapper.checkmarx.cli;

import com.beust.jcommander.Parameter;

public class CheckmarxWrapperCLIParameters {
    @Parameter(names = { "help", "--help" }, description = "Help. Shows help", required = false)
    boolean helpOutputNecessary;

    @Parameter(description = "Action")
    String action;

    public boolean isHelpOutputNecessary() {
        return helpOutputNecessary;
    }

    public String getAction() {
        return action;
    }
}
