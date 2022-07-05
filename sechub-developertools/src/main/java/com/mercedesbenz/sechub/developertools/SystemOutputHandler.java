// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools;

public class SystemOutputHandler implements OutputHandler {

    @Override
    public void output(String text) {
        System.out.println(text);
    }

    @Override
    public void error(String message, Throwable t) {
        System.err.println(message);
        if (t != null) {
            t.printStackTrace();
        }
    }

}
