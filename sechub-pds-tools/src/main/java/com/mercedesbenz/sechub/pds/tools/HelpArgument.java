// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import com.beust.jcommander.Parameter;

public class HelpArgument {

    @Parameter(names = { "--help", "-h" }, help = true)
    boolean help;

}
