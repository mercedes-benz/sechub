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

    /**
     * This is important! In our helm charts for PDS and SecHub server we ensure to
     * use UTF-8. If we would not provide LANG as white listed, every script would
     * need to do a "export LANG=C.UTF-8" to avoid encoding problems (e.g. with file
     * names)
     */
    LANG,

    /*
     * With this variable we can provide special log appenders for java wrapper
     * applications - e.g logstash appenders
     */
    LOGGING_TYPE,

    ;

}
