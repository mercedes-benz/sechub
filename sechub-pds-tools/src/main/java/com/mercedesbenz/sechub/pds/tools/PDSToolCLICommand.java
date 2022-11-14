// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import java.util.List;

public interface PDSToolCLICommand {

    public String getCommandString();

    public String getDescription();

    public List<PDSToolCLICommandArgument> getArguments();
}
