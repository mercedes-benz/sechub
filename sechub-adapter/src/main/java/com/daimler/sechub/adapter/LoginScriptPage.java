// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.LinkedList;
import java.util.List;

public class LoginScriptPage {
    private List<LoginScriptAction> actions = new LinkedList<>();

    public List<LoginScriptAction> getActions() {
        return actions;
    }
}
