// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

public class TestWorkaroundSupport extends SarifImportProductWorkaroundSupport {

    public TestWorkaroundSupport() {
        // here we add the workarounds to
        // simulate spring injection (which we do not have here)
        workarounds.add(new GitleaksSarifImportWorkaround());
    }
}