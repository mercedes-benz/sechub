// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

/**
 * These enumeration represents variable names which are white listed per
 * default by {@link PDSScriptEnvironmentCleaner#clean(java.util.Map)}.
 *
 * @author Albert Tregnaghi
 *
 */
public enum PDSDefaulScriptEnvironmentVariableWhitelist {

    HOME,

    HOSTNAME,

    PATH,

    PWD,

    TERM,

    UID,

    USER,

    /*
     * With this variable we can provide special log appenders for java wrapper
     * applications - e.g logstash appenders
     */
    LOGGING_TYPE,

    ;

}
