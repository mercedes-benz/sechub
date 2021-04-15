// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import java.io.InputStream;

import com.daimler.sechub.adapter.AbstractCodeScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractCodeScanAdapterConfigBuilder;

public class CheckmarxConfig extends AbstractCodeScanAdapterConfig implements CheckmarxAdapterConfig {

    public static final String DEFAULT_CLIENT_SECRET = CheckmarxConstants.DEFAULT_CLIENT_SECRET;

    private String teamIdForNewProjects;
    private InputStream sourceCodeZipFileInputStream;
    public Long presetIdForNewProjects;
    private String clientSecret;// client secret just ensures it is a checkmarx instance - we use default value,
                                // but we make it configurable if this changes ever in future

    private String engineConfigurationName;

    private boolean alwaysFullScanEnabled;

    private CheckmarxConfig() {
    }

    @Override
    public String getTeamIdForNewProjects() {
        return teamIdForNewProjects;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    public Long getPresetIdForNewProjectsOrNull() {
        return presetIdForNewProjects;
    }

    @Override
    public InputStream getSourceCodeZipFileInputStream() {
        return sourceCodeZipFileInputStream;
    }

    @Override
    public String getEngineConfigurationName() {
        return engineConfigurationName;
    }

    public boolean isAlwaysFullScanEnabled() {
        return alwaysFullScanEnabled;
    }

    public static CheckmarxConfigBuilder builder() {
        return new CheckmarxConfigBuilder();
    }

    public static class CheckmarxConfigBuilder extends AbstractCodeScanAdapterConfigBuilder<CheckmarxConfigBuilder, CheckmarxConfig> {

        private String teamIdForNewProjects;
        private Long presetIdForNewProjects;
        private InputStream sourceCodeZipFileInputStream;

        private String clientSecret = DEFAULT_CLIENT_SECRET; // per default use default client secret

        private String engineConfigurationName = CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME;
        private boolean alwaysFullScanEnabled;

        /**
         * When we create a new project this is the team ID to use
         * 
         * @param teamId
         * @return
         */
        public CheckmarxConfigBuilder setTeamIdForNewProjects(String teamId) {
            this.teamIdForNewProjects = teamId;
            return this;
        }

        public CheckmarxConfigBuilder setClientSecret(String newClientSecret) {
            this.clientSecret = newClientSecret;
            return this;
        }

        public CheckmarxConfigBuilder setEngineConfigurationName(String engineConfigurationName) {
            this.engineConfigurationName = engineConfigurationName;
            return this;
        }

        /**
         * When we create a new project this is the team ID to use
         * 
         * @param teamId
         * @return
         */
        public CheckmarxConfigBuilder setPresetIdForNewProjects(Long presetId) {
            this.presetIdForNewProjects = presetId;
            return this;
        }

        public CheckmarxConfigBuilder setSourceCodeZipFileInputStream(InputStream sourceCodeZipFileInputStream) {
            this.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            return this;
        }

        public CheckmarxConfigBuilder setAlwaysFullScan(boolean alwaysFullScanEnabled) {
            this.alwaysFullScanEnabled = alwaysFullScanEnabled;
            return this;
        }

        @Override
        protected void customBuild(CheckmarxConfig config) {
            config.teamIdForNewProjects = teamIdForNewProjects;
            config.presetIdForNewProjects = presetIdForNewProjects;
            config.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            config.clientSecret = clientSecret;
            config.engineConfigurationName = engineConfigurationName;
            config.alwaysFullScanEnabled = alwaysFullScanEnabled;
        }

        @Override
        protected CheckmarxConfig buildInitialConfig() {
            return new CheckmarxConfig();
        }

        @Override
        protected void customValidate() {
            assertUserSet();
            assertPasswordSet();
            assertProjectIdSet();
            assertTeamIdSet();
        }

        protected void assertTeamIdSet() {
            if (teamIdForNewProjects == null) {
                throw new IllegalStateException("no team id given");
            }
        }

    }
}
