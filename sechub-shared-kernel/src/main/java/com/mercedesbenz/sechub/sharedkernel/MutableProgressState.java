// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

public class MutableProgressState implements ProgressState {

    private boolean suspended;
    private boolean canceled;

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

}
