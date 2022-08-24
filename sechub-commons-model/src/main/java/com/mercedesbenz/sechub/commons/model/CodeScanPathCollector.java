// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeScanPathCollector {

    private static final Logger LOG = LoggerFactory.getLogger(CodeScanPathCollector.class);

    private SecHubDataConfigurationObjectInfoFinder configObjectFinder;

    public CodeScanPathCollector() {
        this.configObjectFinder = new SecHubDataConfigurationObjectInfoFinder();
    }

    public Set<String> collectAllCodeScanPathes(SecHubConfigurationModel configuration) {
        Set<String> paths = new LinkedHashSet<>();
        Optional<SecHubCodeScanConfiguration> codeScanOpt = configuration.getCodeScan();
        if (!codeScanOpt.isPresent()) {
            return paths;
        }
        SecHubCodeScanConfiguration codeScan = codeScanOpt.get();
        addFileSystemParts(paths, codeScan);
        Set<String> usedNames = codeScan.getNamesOfUsedDataConfigurationObjects();
        if (usedNames.isEmpty()) {
            return paths;
        }
        List<SecHubDataConfigurationObjectInfo> found = configObjectFinder.findDataObjectsByName(configuration, usedNames);
        for (SecHubDataConfigurationObjectInfo info : found) {
            if (info.getType() != SecHubDataConfigurationType.SOURCE) {
                continue;
            }
            SecHubDataConfigurationObject config = info.getDataConfigurationObject();
            if (!(config instanceof SecHubSourceDataConfiguration)) {
                LOG.warn("source object data was not expected {} but {}", SecHubSourceDataConfiguration.class, config.getClass());
                continue;
            }
            SecHubSourceDataConfiguration sourceDataConfig = (SecHubSourceDataConfiguration) config;
            addFileSystemParts(paths, sourceDataConfig);
        }
        return paths;
    }

    private void addFileSystemParts(Set<String> paths, SecHubFileSystemContainer container) {
        Optional<SecHubFileSystemConfiguration> fileSystemOpt = container.getFileSystem();

        if (!fileSystemOpt.isPresent()) {
            return;
        }
        SecHubFileSystemConfiguration fileSystem = fileSystemOpt.get();

        paths.addAll(fileSystem.getFiles());
        paths.addAll(fileSystem.getFolders());
    }

}
