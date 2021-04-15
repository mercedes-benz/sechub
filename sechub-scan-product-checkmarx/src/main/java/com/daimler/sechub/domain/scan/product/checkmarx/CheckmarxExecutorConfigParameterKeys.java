// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

/**
 * A simple class containing some keys used to executor configuration parameters
 * 
 * @author Albert Tregnaghi
 *
 */
public class CheckmarxExecutorConfigParameterKeys {

    public static final String CHECKMARX_FULLSCAN_ALWAYS = "checkmarx.fullscan.always";
    
    /**
     * Normally only the default client "secret" - which is a static value.
     */
    public static final String CHECKMARX_CLIENT_SECRET = "checkmarx.clientSecret";

    /**
     * Checkmarx engine configuration, for possible values refer to:
     * https://checkmarx.atlassian.net/wiki/spaces/KC/pages/223543515/Get+All+Engine+Configurations+-+GET+sast+engineConfigurations+v8.6.0+and+up
     */
    public static final String CHECKMARX_ENGINE_CONFIGURATIONNAME = "checkmarx.engineConfigurationName";

}
