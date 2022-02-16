// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

public class NullProgressMonitor implements ProgressMonitor {

    @Override
    public boolean isCanceled() {
        return false;
    }

}
