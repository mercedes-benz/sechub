// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;

/**
 * Provides keys which are interpreted <strong>at PDS side</strong>. When the
 * given key (ExecutionPDSKey) is marked as available inside script, the
 * launcher scripts will have the data injected automatically as environment
 * variables.
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
            "When 'true' the SecHub storage will be reused by the PDS server. In this case SecHub will not upload job data to PDS.\n"
                    + "But it's crucial to have the same root storage setup on the PDS server side (e.g. same s3 bucket for S3 storage, or same NFS base for shared volumes).\n"
                    + "When not 'true' or not defined, PDS will use its own storage locations")
            .markSendToPDS().markDefaultRecommended().withDefault(true))

    ,
    /**
     * Special (optional) key inside executor configuration which will be used to
     * define if PDS will use SecHub mappings. The value contains a comma separated
     * list of mapping ids.
     */
    PDS_CONFIG_USE_SECHUB_MAPPINGS(
            new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_MAPPINGS, "Contains a comma separated list of mappping ids. "
                    + "Each defined mapping will be fetched from SecHub DB as JSON and sent as job parameter with " + "the mapping id as name to the PDS.")
                    .markSendToPDS().withDefault(true))

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
            "This contains a comma separated list of path patterns for file includes. These patterns can contain wildcards. Matching will be done case insensitive!\n"
                    + "Every file which is matched by one of the patterns will be included - except those which are explicitly excluded.\n"
                    + "When nothing is defined, then every content is accepted for include.\n\n"
                    + "For example: '*.go,*.html, test1.txt' would include every Go file, every HTML file and files named 'test1.txt'.")
            .markSendToPDS()),

    /**
     * Contains trust all certificates information
     */
    PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED(new ExecutionPDSKey(
            PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED,
            "When 'true' the PDS adapter script used by the job will have the information and can use this information when it comes to remote operations.")
            .markSendToPDS().markAsAvailableInsideScript().markDefaultRecommended().withDefault(false)),

    PDS_CONFIG_SUPPORTED_DATATYPES(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES,
            "Can be " + SecHubDataConfigurationType.SOURCE + ", " + SecHubDataConfigurationType.BINARY + ", " + SecHubDataConfigurationType.NONE
                    + " or a combination as a comma separated list. This data should"
                    + " normally not be defined via a default value of an optional PDS configuration parameter.")
            .markSendToPDS()),

    PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRIES_MAX(
            new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRIES_MAX,
                    "Defines the maximum amount of retries done when a job storage read access is failing").markSendToPDS()),

    PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRY_WAIT_SECONDS(
            new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRY_WAIT_SECONDS,
                    "Defines the time to wait in seconds before the next retry is done (when it comes to storage READ problems)").markSendToPDS()),

    /**
     * Contains product timeout information
     */
    PDS_CONFIG_TIMEOUT_PRODUCT_MINUTES(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES,
            "Maximum allowed time in minutes, before a product will time out - this means that the launcher script is automatically canceled by PDS")
            .markSendToPDS()),

    /**
     * Contains file filter exclude information
     */
    PDS_CONFIG_FILEFILTER_EXCLUDES(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_FILEFILTER_EXCLUDES,
            "This contains a comma separated list of path patterns for file excludes. These patterns can contain wildcards. Matching will be done case insensitive!\n"
                    + "When empty, then nothing will be excluded. The exclude operation will be done AFTER the include file filtering.\n\n"
                    + "For example: '*.go,*.html, test1.txt' would exclude every Go file, every HTML file and files named 'test1.txt'.")
            .markSendToPDS()),

    /**
     * This is automatically given to PDS by SecHub - depending on the scan type.
     * E.g. for a webscan this will be used to identify the current webscan target
     * URL to start scanning.
     */
    PDS_SCAN_TARGET_URL(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_URL,
            "This contains the target URL for the current scan (i.e. webscan). Will not be set in all scan types. E.g. for a code scan this environment variable will not be available")
            .markGenerated().markAsAvailableInsideScript()),

    /**
     * A debug flag for PDS which can be used in executor configurations to increase
     * output level on scan time.
     */
    PDS_DEBUG_ENABLED(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_DEBUG_ENABLED,
            "When 'true', the PDS instance will show up some debug information on scan time. The output level of debugging information differs on PDS solutions/launcher scripts.")
            .markDefaultRecommended().withDefault(false).markSendToPDS().markAsAvailableInsideScript())

    ,
    /**
     * A flag for PDS scripts which are calling a wrapper application. The script
     * can use this information to start the wrapper application with a remote debug
     * connection. Here an example for a java based wrapper application:
     *
     * <pre>
     * <code>
     * if [[ "$PDS_WRAPPER_REMOTE_DEBUGGING_ENABLED" = "true" ]]; then
     *    options="$options -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
     * fi
     * java -jar $options "$prepare_wrapper"
     *  <code>
     * </pre>
     */
    PDS_WRAPPER_REMOTE_DEBUGGING_ENABLED(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_WRAPPER_REMOTE_DEBUGGING_ENABLED,
            "Additional information will be always sent to launcher scripts. Interesting to debug wrapper applications remote.").markDefaultRecommended()
            .withDefault(false).markSendToPDS().markAsAvailableInsideScript())

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

    PDS_CONFIG_CANCEL_EVENT_CHECKINTERVAL_MILLISECONDS(new ExecutionPDSKey(
            PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_CANCEL_EVENT_CHECKINTERVAL_MILLISECONDS,
            "This is the maximum time the launcher script process will be kept alive after a cancellation event is sent. "
                    + "This gives the launcher script process a chance to recognize the cancel event and do some final cancel parts and exit gracefully.")
            .markSendToPDS()),

    PDS_CONFIG_CANCEL_MAXIMUM_WAITTIME_SECOND(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_CANCEL_MAXIMUM_WAITTIME_SECONDS,
            "The time in seconds PDS will check again if the launcher script process is alive or not when the process shall be canceled. When nothing defined, the default value is:"
                    + PDSDefaultParameterValueConstants.DEFAULT_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION + ". If the value is "
                    + PDSDefaultParameterValueConstants.NO_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION + " the process will be terminated without waiting.")
            .markSendToPDS()),

    /**
     * A special runtime configuration configuration for PDS servers started with
     * mocked profile: Normally every PDS call will result in a real execution - no
     * matter if products shall be mocked or not. Reason: We use always a real PDS
     * server to communicate and normally do not want any mocks here. But when this
     * is parameter is set to <code>false</code>, a mock will even be used for PDS.
     */
    PDS_MOCKING_DISABLED(new ExecutionPDSKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_MOCKING_DISABLED,
            "When 'true' any PDS adapter call will use real PDS adapter and not a mocked variant.").markForTestingOnly().markSendToPDS()
            .markDefaultRecommended().withDefault(true));

    private ExecutionPDSKey key;

    PDSConfigDataKeyProvider(ExecutionPDSKey key) {
        this.key = key;
    }

    public ExecutionPDSKey getKey() {
        return key;
    }

}
