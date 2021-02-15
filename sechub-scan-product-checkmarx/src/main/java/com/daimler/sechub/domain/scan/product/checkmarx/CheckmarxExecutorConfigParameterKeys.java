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
     * Checkmarx engine configuration, for possible values refer
     * https://checkmarx.atlassian.net/wiki/spaces/KC/pages/223543515/Get+All+Engine+Configurations+-+GET+sast+engineConfigurations+v8.6.0+and+up
     */
    public static final String CHECKMARX_ENGINE_CONFIGURATIONNAME = "checkmarx.engineConfigurationName";

}
