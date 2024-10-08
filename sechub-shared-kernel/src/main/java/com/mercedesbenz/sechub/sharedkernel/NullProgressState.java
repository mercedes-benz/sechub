// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

public class NullProgressState implements ProgressState {

    public static final NullProgressState INSTANCE = new NullProgressState();

    private NullProgressState() {
        /* we want only the shared INSTANCE */
    }

    @Override
    public boolean isSuspended() {
        return false;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

}
