package com.mercedesbenz.sechub.wrapper.checkmarx.cli;

import com.beust.jcommander.Parameter;

public class CheckmarxWrapperCLIParameters {
    @Parameter(names = { "help", "--help" }, description = "Help. Shows help", required = false)
    boolean helpOutputNecessary;

    public boolean isHelpOutputNecessary() {
        return helpOutputNecessary;
    }
}
