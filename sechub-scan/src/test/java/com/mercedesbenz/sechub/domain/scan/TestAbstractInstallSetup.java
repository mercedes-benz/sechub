// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

public class TestAbstractInstallSetup extends AbstractInstallSetup {

    public boolean canScanDaimlerIntranet;
    public boolean canScanInternet;

    @Override
    protected void init(ScanInfo info) {
        info.canScanIntranet = canScanDaimlerIntranet;
        info.canScanInternet = canScanInternet;
    }

}