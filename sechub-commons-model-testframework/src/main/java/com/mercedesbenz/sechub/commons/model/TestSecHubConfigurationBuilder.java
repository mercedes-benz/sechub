// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.login.TestWebLoginConfigurationBuilder;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;

public class TestSecHubConfigurationBuilder {

    private Data data;

    public static final TestSecHubConfigurationBuilder configureSecHub() {
        return new TestSecHubConfigurationBuilder();
    }

    private TestSecHubConfigurationBuilder() {
        this.data = new Data();
    }

    public SecHubScanConfiguration build() {
        SecHubScanConfiguration result = new SecHubScanConfiguration();

        result.setApiVersion(data.version);
        result.setInfraScan(data.infraConfig);
        result.setWebScan(data.webConfig);
        result.setProjectId(data.projectId);
        result.setCodeScan(data.codeScanConfig);

        data = new Data();
        return result;
    }

    private class Data {
        private String version;
        private SecHubWebScanConfiguration webConfig;
        private SecHubInfrastructureScanConfiguration infraConfig;
        private SecHubCodeScanConfiguration codeScanConfig;
        private String projectId;
    }

    public TestSecHubConfigurationBuilder api(String version) {
        this.data.version = version;
        return this;
    }

    public TestWebConfigurationBuilder webConfig() {
        return new TestWebConfigurationBuilder();
    }

    public class TestWebConfigurationBuilder {
        private TestWebConfigurationBuilder() {
            TestSecHubConfigurationBuilder.this.data.webConfig = new SecHubWebScanConfiguration();
        }

        public SecHubScanConfiguration build() {
            return TestSecHubConfigurationBuilder.this.build();
        }

        public TestSecHubConfigurationBuilder and() {
            return TestSecHubConfigurationBuilder.this;
        }

        public TestWebConfigurationBuilder login(WebLoginConfiguration loginConfig) {
            TestSecHubConfigurationBuilder.this.data.webConfig.login = Optional.ofNullable(loginConfig);
            return this;
        }

        public TestWebConfigurationBuilder addURI(String uri) {
            TestSecHubConfigurationBuilder.this.data.webConfig.uri = URI.create(uri);
            return this;
        }

        public TestWebConfigurationBuilder maxScanDuration(WebScanDurationConfiguration maxScanDuration) {
            TestSecHubConfigurationBuilder.this.data.webConfig.maxScanDuration = Optional.ofNullable(maxScanDuration);
            return this;
        }

        public TestWebConfigurationBuilder addIncludes(List<String> includes) {
            TestSecHubConfigurationBuilder.this.data.webConfig.includes = Optional.ofNullable(includes);
            return this;
        }

        public TestWebConfigurationBuilder addExcludes(List<String> excludes) {
            TestSecHubConfigurationBuilder.this.data.webConfig.excludes = Optional.ofNullable(excludes);
            return this;
        }

        public TestWebLoginConfigurationBuilder login(String loginURL) {
            return new TestWebLoginConfigurationBuilder(loginURL, this);
        }
    }

    public TestCodeSCanConfigurationBuilder codeScanConfig() {
        return new TestCodeSCanConfigurationBuilder();
    }

    public class TestCodeSCanConfigurationBuilder {

        private TestCodeSCanConfigurationBuilder() {
            TestSecHubConfigurationBuilder.this.data.codeScanConfig = new SecHubCodeScanConfiguration();
        }

        public SecHubScanConfiguration build() {
            return TestSecHubConfigurationBuilder.this.build();
        }

        public TestSecHubConfigurationBuilder and() {
            return TestSecHubConfigurationBuilder.this;
        }

        public TestCodeSCanConfigurationBuilder setFileSystemFolders(String... folders) {
            SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
            TestSecHubConfigurationBuilder.this.data.codeScanConfig.setFileSystem(fileSystem);
            fileSystem.getFolders().addAll(Arrays.asList(folders));
            return this;
        }
    }

    public TestInfraConfigurationBuilder infraConfig() {
        return new TestInfraConfigurationBuilder();
    }

    public class TestInfraConfigurationBuilder {
        private TestInfraConfigurationBuilder() {
            TestSecHubConfigurationBuilder.this.data.infraConfig = new SecHubInfrastructureScanConfiguration();
        }

        public SecHubScanConfiguration build() {
            return TestSecHubConfigurationBuilder.this.build();
        }

        public TestSecHubConfigurationBuilder and() {
            return TestSecHubConfigurationBuilder.this;
        }

        public TestInfraConfigurationBuilder addURI(String uri) {
            TestSecHubConfigurationBuilder.this.data.infraConfig.getUris().add(URI.create(uri));
            return this;
        }

        public TestInfraConfigurationBuilder addIP(String ip) {
            try {
                TestSecHubConfigurationBuilder.this.data.infraConfig.getIps().add(InetAddress.getByName(ip));
            } catch (UnknownHostException e) {
                throw new IllegalStateException("Unknown host - should not happen in testcase. Seems to be infrastructure problem!", e);
            }
            return this;
        }
    }

    public TestSecHubConfigurationBuilder projectId(String projectId) {
        TestSecHubConfigurationBuilder.this.data.projectId = projectId;
        return this;
    }
}
