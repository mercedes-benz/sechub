// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.TestSecHubConfigurationBuilder.TestDataConfigurationBuilder.TestDataBinaryBuilder;
import com.mercedesbenz.sechub.commons.model.TestSecHubConfigurationBuilder.TestDataConfigurationBuilder.TestDataSourceBuilder;
import com.mercedesbenz.sechub.commons.model.login.TestWebLoginConfigurationBuilder;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;

public class TestSecHubConfigurationBuilder {

    private TestData testData;

    public static final TestSecHubConfigurationBuilder configureSecHub() {
        return new TestSecHubConfigurationBuilder();
    }

    private TestSecHubConfigurationBuilder() {
        this.testData = new TestData();
    }

    public TestDataConfigurationBuilder data() {
        testData.data = new TestDataConfigurationBuilder();
        return testData.data;
    }

    public SecHubScanConfiguration build() {
        SecHubScanConfiguration result = new SecHubScanConfiguration();

        result.setApiVersion(testData.version);
        result.setInfraScan(testData.infraConfig);
        result.setWebScan(testData.webConfig);
        result.setProjectId(testData.projectId);
        result.setCodeScan(testData.codeScanConfig);
        result.setLicenseScan(testData.licenseScanConfig);
        result.setSecretScan(testData.secretScanConfig);

        if (testData.data != null) {

            SecHubDataConfiguration dataConfiguration = new SecHubDataConfiguration();
            result.setData(dataConfiguration);

            List<TestDataSourceBuilder> sourceBuilders = testData.data.sourceDataBuilders;
            for (TestDataSourceBuilder builder : sourceBuilders) {
                SecHubSourceDataConfiguration sourceConfig = new SecHubSourceDataConfiguration();
                sourceConfig.setUniqueName(builder.getName());
                sourceConfig.setFileSystem(builder.getFileSystem());

                dataConfiguration.getSources().add(sourceConfig);
            }

            List<TestDataBinaryBuilder> binaryBuilders = testData.data.binaryDataBuilders;
            for (TestDataBinaryBuilder builder : binaryBuilders) {
                SecHubBinaryDataConfiguration binaryConfig = new SecHubBinaryDataConfiguration();
                binaryConfig.setUniqueName(builder.getName());
                binaryConfig.setFileSystem(builder.getFileSystem());

                dataConfiguration.getBinaries().add(binaryConfig);
            }

        }

        testData = new TestData();
        return result;
    }

    private class TestData {
        public TestDataConfigurationBuilder data;
        private String version;
        private SecHubWebScanConfiguration webConfig;
        private SecHubInfrastructureScanConfiguration infraConfig;
        private SecHubCodeScanConfiguration codeScanConfig;
        private SecHubSecretScanConfiguration secretScanConfig;
        private SecHubLicenseScanConfiguration licenseScanConfig;
        private String projectId;
    }

    public TestSecHubConfigurationBuilder api(String version) {
        this.testData.version = version;
        return this;
    }

    public TestWebConfigurationBuilder webConfig() {
        return new TestWebConfigurationBuilder();
    }

    public class TestWebConfigurationBuilder {

        private TestWebConfigurationBuilder() {
            TestSecHubConfigurationBuilder.this.testData.webConfig = new SecHubWebScanConfiguration();
        }

        public SecHubScanConfiguration build() {
            return TestSecHubConfigurationBuilder.this.build();
        }

        public TestSecHubConfigurationBuilder and() {
            return TestSecHubConfigurationBuilder.this;
        }

        public TestWebConfigurationBuilder login(WebLoginConfiguration loginConfig) {
            TestSecHubConfigurationBuilder.this.testData.webConfig.login = Optional.ofNullable(loginConfig);
            return this;
        }

        public TestWebConfigurationBuilder addURI(String uri) {
            TestSecHubConfigurationBuilder.this.testData.webConfig.url = URI.create(uri);
            return this;
        }

        public TestWebConfigurationBuilder maxScanDuration(WebScanDurationConfiguration maxScanDuration) {
            TestSecHubConfigurationBuilder.this.testData.webConfig.maxScanDuration = Optional.ofNullable(maxScanDuration);
            return this;
        }

        public TestWebConfigurationBuilder addIncludes(List<String> includes) {
            TestSecHubConfigurationBuilder.this.testData.webConfig.includes = Optional.ofNullable(includes);
            return this;
        }

        public TestWebConfigurationBuilder addExcludes(List<String> excludes) {
            TestSecHubConfigurationBuilder.this.testData.webConfig.excludes = Optional.ofNullable(excludes);
            return this;
        }

        public TestWebConfigurationBuilder addHeaders(List<HTTPHeaderConfiguration> headers) {
            TestSecHubConfigurationBuilder.this.testData.webConfig.headers = Optional.ofNullable(headers);
            return this;
        }

        public TestWebLoginConfigurationBuilder login(String loginURL) {
            return new TestWebLoginConfigurationBuilder(loginURL, this);
        }

        public TestWebConfigurationBuilder addApiConfig(SecHubWebScanApiConfiguration apiConfig) {
            TestSecHubConfigurationBuilder.this.testData.webConfig.api = Optional.ofNullable(apiConfig);
            return this;
        }

        public TestWebConfigurationBuilder addClientCertificateConfig(ClientCertificateConfiguration clientCertificateConfig) {
            TestSecHubConfigurationBuilder.this.testData.webConfig.clientCertificate = Optional.ofNullable(clientCertificateConfig);
            return this;
        }

        public TestWebConfigurationBuilder logout(WebLogoutConfiguration logoutConfig) {
            TestSecHubConfigurationBuilder.this.testData.webConfig.logout = logoutConfig;
            return this;
        }
    }

    public TestSecretScanConfigurationBuilder secretScanConfig() {
        return new TestSecretScanConfigurationBuilder();
    }

    public class TestSecretScanConfigurationBuilder {

        private TestSecretScanConfigurationBuilder() {
            TestSecHubConfigurationBuilder.this.testData.secretScanConfig = new SecHubSecretScanConfiguration();
        }

        public SecHubScanConfiguration build() {
            return TestSecHubConfigurationBuilder.this.build();
        }

        public TestSecHubConfigurationBuilder and() {
            return TestSecHubConfigurationBuilder.this;
        }

        public TestSecretScanConfigurationBuilder useDataReferences(String... referenceName) {
            testData.secretScanConfig.getNamesOfUsedDataConfigurationObjects().addAll(Arrays.asList(referenceName));
            return this;
        }

    }

    public TestLicenseScanConfigurationBuilder licenseScanConfig() {
        return new TestLicenseScanConfigurationBuilder();
    }

    public class TestLicenseScanConfigurationBuilder {

        private TestLicenseScanConfigurationBuilder() {
            TestSecHubConfigurationBuilder.this.testData.licenseScanConfig = new SecHubLicenseScanConfiguration();
        }

        public SecHubScanConfiguration build() {
            return TestSecHubConfigurationBuilder.this.build();
        }

        public TestSecHubConfigurationBuilder and() {
            return TestSecHubConfigurationBuilder.this;
        }

        public TestLicenseScanConfigurationBuilder useDataReferences(String... referenceName) {
            testData.licenseScanConfig.getNamesOfUsedDataConfigurationObjects().addAll(Arrays.asList(referenceName));
            return this;
        }

    }

    public TestCodeSCanConfigurationBuilder codeScanConfig() {
        return new TestCodeSCanConfigurationBuilder();
    }

    public class TestCodeSCanConfigurationBuilder {

        private TestCodeSCanConfigurationBuilder() {
            TestSecHubConfigurationBuilder.this.testData.codeScanConfig = new SecHubCodeScanConfiguration();
        }

        public SecHubScanConfiguration build() {
            return TestSecHubConfigurationBuilder.this.build();
        }

        public TestSecHubConfigurationBuilder and() {
            return TestSecHubConfigurationBuilder.this;
        }

        public TestCodeSCanConfigurationBuilder useDataReferences(String... referenceName) {
            testData.codeScanConfig.getNamesOfUsedDataConfigurationObjects().addAll(Arrays.asList(referenceName));
            return this;
        }

        public TestCodeSCanConfigurationBuilder setFileSystemFolders(String... folders) {
            SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
            TestSecHubConfigurationBuilder.this.testData.codeScanConfig.setFileSystem(fileSystem);
            fileSystem.getFolders().addAll(Arrays.asList(folders));
            return this;
        }
    }

    public class TestDataConfigurationBuilder {

        private List<TestDataBinaryBuilder> binaryDataBuilders = new ArrayList<>();
        private List<TestDataSourceBuilder> sourceDataBuilders = new ArrayList<>();

        private TestDataConfigurationBuilder() {
        }

        public SecHubDataConfiguration build() {
            return TestDataConfigurationBuilder.this.build();
        }

        public TestDataConfigurationBuilder also() {
            return TestDataConfigurationBuilder.this;
        }

        public TestDataBinaryBuilder withBinary() {
            TestDataBinaryBuilder binary = new TestDataBinaryBuilder();
            binaryDataBuilders.add(binary);
            return binary;
        }

        public TestDataSourceBuilder withSource() {
            TestDataSourceBuilder source = new TestDataSourceBuilder();
            sourceDataBuilders.add(source);
            return source;
        }

        public TestSecHubConfigurationBuilder and() {
            return TestSecHubConfigurationBuilder.this;
        }

        public abstract class TestDataDataBuilder {
            private String name;
            private SecHubFileSystemConfiguration fileSystem;

            private TestDataDataBuilder() {
            }

            public TestDataDataBuilder uniqueName(String name) {
                this.name = name;
                return this;
            }

            private SecHubFileSystemConfiguration ensureFileSystem() {
                if (fileSystem == null) {
                    fileSystem = new SecHubFileSystemConfiguration();
                }
                return fileSystem;
            }

            public TestDataDataBuilder fileSystemFolders(String... folders) {
                ensureFileSystem().getFolders().addAll(Arrays.asList(folders));
                return this;
            }

            public TestDataDataBuilder fileSystemFiles(String... files) {
                ensureFileSystem().getFiles().addAll(Arrays.asList(files));
                return this;
            }

            public String getName() {
                return name;
            }

            public SecHubFileSystemConfiguration getFileSystem() {
                return fileSystem;
            }

            public TestDataConfigurationBuilder end() {
                return TestDataConfigurationBuilder.this;
            }

        }

        public class TestDataBinaryBuilder extends TestDataDataBuilder {
            private TestDataBinaryBuilder() {

            }

        }

        public class TestDataSourceBuilder extends TestDataDataBuilder {
            private TestDataSourceBuilder() {

            }

        }

    }

    public TestInfraConfigurationBuilder infraConfig() {
        return new TestInfraConfigurationBuilder();
    }

    public class TestInfraConfigurationBuilder {
        private TestInfraConfigurationBuilder() {
            TestSecHubConfigurationBuilder.this.testData.infraConfig = new SecHubInfrastructureScanConfiguration();
        }

        public SecHubScanConfiguration build() {
            return TestSecHubConfigurationBuilder.this.build();
        }

        public TestSecHubConfigurationBuilder and() {
            return TestSecHubConfigurationBuilder.this;
        }

        public TestInfraConfigurationBuilder addURI(String uri) {
            TestSecHubConfigurationBuilder.this.testData.infraConfig.getUris().add(URI.create(uri));
            return this;
        }

        public TestInfraConfigurationBuilder addIP(String ip) {
            try {
                TestSecHubConfigurationBuilder.this.testData.infraConfig.getIps().add(InetAddress.getByName(ip));
            } catch (UnknownHostException e) {
                throw new IllegalStateException("Unknown host - should not happen in testcase. Seems to be infrastructure problem!", e);
            }
            return this;
        }
    }

    public TestSecHubConfigurationBuilder projectId(String projectId) {
        TestSecHubConfigurationBuilder.this.testData.projectId = projectId;
        return this;
    }
}
