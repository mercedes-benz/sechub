// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.util.List;

public interface LoginScriptGenerator {

    public String generate(List<LoginScriptAction> actions);
}
