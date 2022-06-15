// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

/**
 * Provides keys which are interpreted <strong>at PDS side</strong>.
 *
 * <h3>Important</h3> The enum values MUST have same name as the env variables
 * provided to PDS script! <i>(makes it easier to understand, to maintain
 * etc.)</i>
 *
 * @author Albert Tregnaghi
 *
 */
public enum PDSConfigDataKeyProvider implements PDSKeyProvider<ExecutionPDSKey> {

    /**
     * Special key inside executor configuration which will be used to define the
     * PDS product identifier! So this key is not inside job parameters, but will be
     * available as "productID" for PDS
     */
    PDS_CONFIG_PRODUCTIDENTIFIER(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_PRODUCTIDENTIFIER,
            "Contains the product identifier, so PDS knows which part is to call on it's side.").markMandatory().markAsAvailableInsideScript()),

    /**
     * This is automatically given to PDS by SecHub on every call by adapter
     */
    PDS_SCAN_TARGET_TYPE(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_TYPE,
            "Contains the target type (depending on scan type) and will be just an additional information for PDS from SecHub.").markGenerated()
                    .markAsAvailableInsideScript()),

    /**
     * Special key inside executor configuration which will be used to define if PDS
     * will reuse SecHub storage locations. When storage location is reused the PDS
     * executor will not upload job data to PDS again and PDS server will reuse the
     * SecHub storage directly.
     */
    PDS_CONFIG_USE_SECHUB_STORAGE(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE,
            "When 'true' the SecHub storage will be reused by PDS server. In this case SecHub will not upload job data to PDS.\n"
                    + "But it's crucial to have same root storage setup on PDS server side (e.g. same s3 bucket for S3 storage, or same NFS base for shared volumes).\n"
                    + "When not `true` or not defined, pds will use its own storage locations").markAlwaysSentToPDS().markDefaultRecommended()
                            .withDefault(true))

    ,

    /**
     * Contains sechub storage location
     */
    PDS_CONFIG_SECHUB_STORAGE_PATH(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH,
            "This contains the sechub storage location when sechub storage shall be used. So PDS knows location - in combination with sechub job UUID reuse is possible")
                    .markGenerated()),

    /**
     * Contains file filter include information
     */
    PDS_CONFIG_FILEFILTER_INCLUDES(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_FILEFILTER_INCLUDES,
            "This containts a comma separated list of wildcards for file includes (e.g. `*.go,*.html, test1.txt` would include every go file, every HTML file and files named `test1.txt`). When every fill wwill be included - except those which are explicit excluded.")
                    .markAlwaysSentToPDS()),

    /**
     * Contains file filter exclude information
     */
    PDS_CONFIG_FILEFILTER_EXCLUDES(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_FILEFILTER_EXCLUDES,
            "This containts a comma separated list of wildcards for file excludes (e.g. `*.go,*.html, test1.txt` would excluded every go file, every HTML file and files named `test1.txt`). When empty none of the files will be excluded. The exclude operation will be done AFTER the include file filtering has happend.")
                    .markAlwaysSentToPDS()),

    /**
     * This is automatically given to PDS by SecHub - depending on scan type. E.g.
     * for a webscan this will be used to identify the current webscan target URL to
     * start scanning.
     */
    PDS_SCAN_TARGET_URL(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_URL,
            "This contains the Target URL for current scan i.e webscan. Will not be set in all scan types. E.g. for a code scan this environment variable will not be available")
                    .markGenerated().markAsAvailableInsideScript())

    ,
    /**
     * This is automatically given to PDS by SecHub - depending on scan type. E.g.
     * for a webscan the configuration will only contain the configuration for web
     * and the common parts.
     *
     */
    PDS_SCAN_CONFIGURATION(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION,
            "This contains the SecHub configuration as JSON object (but reduced to current scan type, so e.g. a web scan will have no code scan configuration data available")
                    .markGenerated().markAsAvailableInsideScript()),

    /**
     * A special runtime configuration configuration for PDS servers started with
     * mocked profile: Normally every PDS call will result in a real execution - no
     * matter if products shall be mocked or not. Reason: We use always a real PDS
     * server to communicate and normally do not want any mocks here. But when this
     * is parameter is set to <code>false</code>, a mock will even be used for PDS.
     */
    PDS_MOCKING_DISABLED(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_MOCKING_DISABLED,
            "When 'true' any PDS adapter call will use real PDS adapter and not a mocked variant.").markAlwaysSentToPDS().markDefaultRecommended()
                    .withDefault(true));

    private ExecutionPDSKey key;

    PDSConfigDataKeyProvider(ExecutionPDSKey key) {
        this.key = key;
    }

    public ExecutionPDSKey getKey() {
        return key;
    }

}
