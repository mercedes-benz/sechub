// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

public class NullProgressStateFetcher implements ProgressStateFetcher {

    @Override
    public ProgressState fetchProgressState() {
        return NullProgressState.INSTANCE;
    }

}
