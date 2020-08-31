// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

/**
 * Defines ENV variables and corresponding system properites.
 * <br><br>
 * BE AWARE: env entries are exact like names of enum, system properties as defined in concstructor call!
 * So NOT SAME!!! Was done to provide standard env entries of sechub-client but have possiblity to define 
 * different when necessary . System properties are first class citizens so will override ENV variants
 * @author Albert Tregnaghi
 *
 */
public enum ConfigurationSetup {

    SECHUB_ADMIN_USERID(false),

    SECHUB_ADMIN_APITOKEN(false),

    SECHUB_ADMIN_SERVER("sechub.developertools.admin.server", false),

    SECHUB_ADMIN_SERVER_PORT("sechub.developertools.admin.serverport", true),

    SECHUB_ADMIN_SERVER_PROTOCOL("sechub.developertools.admin.serverprotocol", true),

    SECHUB_ENABLE_INTEGRATION_TESTSERVER_MENU("sechub.developertools.admin.integrationtestserver", true),

    SECHUB_DISABLE_CONFIRMATIONS("sechub.developertools.admin.disable.confim", true, "When set to true, no confirmation dialogs will appear"),

    SECHUB_CHECK_STATUS_ON_STARTUP("sechub.developertools.admin.statuscheck.onstartup", true),
    /**
     * Here you can set environment information. See description for details
     */
    SECHUB_ADMIN_ENVIRONMENT("sechub.developertools.admin.environment", false,
            "Use 'PROD', 'INT' or anything containing 'TEST' for dedicated colors (red,yellow,cyan). All other variants are without special colors"),

    SECHUB_MASS_OPERATION_PARENTDIRECTORY("sechub.developertools.admin.massoperation.parentdirectory", true),

    /**
     * Usage: for example -Dsechub.developertools.output.font.settings="courier 18"
     */
    SECHUB_OUTPUT_FONT_SETTINGS("sechub.developertools.output.font.settings", true),

    SECHUB_LOOK_AND_FEEL("sechub.developertools.lookandfeel.nimbus", true),

    SECHUB_TARGETFOLDER_FOR_SECHUB_CLIENT_SCAN("sechub.developertools.admin.launch.scan.targetfolder", true,
            "Default path to parent folder of configuration file and sources to scan"),
    
    /**
     * When defined we use this path instead IDE relative one
     */
    SECHUB_PATH_TO_SECHUB_CLIENT_BINARY("sechub.developertools.admin.launch.clientbinary.path",true),
    
    
    SECHUB_TRUSTALL_DENIED("sechub.developertools.trustall.denied",true);
    ;

    private String systemPropertyid;
    private String environmentEntryId;

    private boolean optional;
    private String description;

    private ConfigurationSetup(boolean optional) {
        this(null, optional);
    }

    private ConfigurationSetup(String systemPropertyid, boolean optional) {
        this(systemPropertyid, optional, null);
    }

    private ConfigurationSetup(String systemPropertyid, boolean optional, String description) {
        this.optional = optional;
        this.systemPropertyid = systemPropertyid;
        this.environmentEntryId = name();
        this.description = description;
    }

    public String getEnvironmentEntryId() {
        return environmentEntryId;
    }

    public String getSystemPropertyid() {
        return systemPropertyid;
    }

    public static boolean isIntegrationTestServerMenuEnabled() {
        return Boolean.getBoolean(ConfigurationSetup.SECHUB_ENABLE_INTEGRATION_TESTSERVER_MENU.getSystemPropertyid());
    }

    public static boolean isConfirmationDisabled() {
        return Boolean.getBoolean(ConfigurationSetup.SECHUB_DISABLE_CONFIRMATIONS.getSystemPropertyid());
    }

    public static boolean isCheckOnStartupEnabled() {
        return Boolean.getBoolean(ConfigurationSetup.SECHUB_CHECK_STATUS_ON_STARTUP.getSystemPropertyid());
    }

    public static String getOutputFontSettings(String defaultSetting) {
        return ConfigurationSetup.SECHUB_OUTPUT_FONT_SETTINGS.getStringValue(defaultSetting);
    }

    public static String getParentFolderPathForSecHubClientScanOrNull() {
        return ConfigurationSetup.SECHUB_TARGETFOLDER_FOR_SECHUB_CLIENT_SCAN.getStringValue(null,false);
     }

    /**
     * 
     * @return <code>true</code> when nimbus look and feel shall be used - per
     *         default not, because NIMBUS leads to problem with JDialog on Linux
     *         GTK
     */
    public static boolean isNimbusLookAndFeelEnabled() {
        return Boolean.getBoolean(ConfigurationSetup.SECHUB_LOOK_AND_FEEL.getSystemPropertyid());
    }
    
    public static boolean isTrustAllDenied() {
        return Boolean.getBoolean(ConfigurationSetup.SECHUB_TRUSTALL_DENIED.getSystemPropertyid());
    }
    
    /**
     * Resolves string value of configuration and fails when not configured
     * 
     * @return value
     * @throws IllegalStateException when value not found
     */
    public String getStringValueOrFail() {
        return getStringValue(null);
    }

    /**
     * Resolves string value of configuration.
     * 
     * @param defaultValue
     * @return value or default value - never <code>null</code>
     * @throws IllegalStateException when value not found and no default value
     *                               available
     */
    public String getStringValue(String defaultValue) {
        return getStringValue(defaultValue, true);
    }

    /**
     * Resolves string value of configuration.
     * 
     * @param defaultValue
     * @param failWhenNull - when true, null is not accepted
     * @return value or default value - never <code>null</code>
     * @throws IllegalStateException when failWhenNull is set to <code>true</code>
     *                               and value not found and no default value
     *                               available
     */
    public String getStringValue(String defaultValue, boolean failWhenNull) {
        String value = null;
        /* first try ENV entry */
        if (environmentEntryId != null) {
            value = System.getenv(environmentEntryId);
        }
        /* then try system property - if not already set */
        if (value == null) {
            if (systemPropertyid != null) {
                value = System.getProperty(systemPropertyid, defaultValue);
            }
        }
        /* then use default value - if not already set */
        if (value == null) {
            value = defaultValue;
        }
        if (failWhenNull) {
            assertNotEmpty(value, name());
        }
        return value;
    }

    private void assertNotEmpty(String part, String missing) {
        if (part == null || part.isEmpty()) {
            throw new IllegalStateException(
                    "Missing configuration entry:" + missing + ".\nYou have to configure these values:" + ConfigurationSetup.description());
        }

    }

    private static String description() {
        StringBuilder sb = new StringBuilder();
        sb.append("Use following system properties:\n");
        for (ConfigurationSetup setup : values()) {
            if (setup.systemPropertyid == null) {
                continue;
            }
            sb.append("-D");
            sb.append(setup.systemPropertyid);
            sb.append("=");
            String val = System.getProperty(setup.systemPropertyid);
            if (val != null && !val.isEmpty()) {
                val = "**** (already set)";
            }
            sb.append(val);
            if (setup.optional) {
                sb.append(" (optional)");
            }
            if (setup.description != null) {
                sb.append(" [");
                sb.append(setup.description);
                sb.append("]");
            }
            sb.append("\n");
        }
        sb.append("\nFor security reasons next parts must be set as environment variables (so not visible in process view):\n");
        for (ConfigurationSetup setup : values()) {
            if (setup.environmentEntryId == null) {
                continue;
            }
            sb.append("  ");
            sb.append(setup.environmentEntryId);
            sb.append("'\n");
        }
        return sb.toString();
    }

   

}
