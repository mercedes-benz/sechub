// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

public class CheckmarxConstants {
    
    /**
     * This is the "client secret" is listed at <a href=
     * "https://checkmarx.atlassian.net/wiki/spaces/KC/pages/1187774721/Using+the+CxSAST+REST+API+v8.6.0+and+up"
     * >public Checkmarx documentation</a>
     *
     * Being not really a secret but just a visible constant in public space it's
     * okay to contain this inside code.
     */
    public static final String DEFAULT_CLIENT_SECRET = "014DF517-39D1-4453-B7B3-9930C563627C";

    public static final String DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME = "Multi-language Scan";
}
