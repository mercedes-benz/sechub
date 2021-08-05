// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.scan.ScanAssertService;
import com.daimler.sechub.domain.scan.project.ScanProjectConfig.ScanProjectConfigCompositeKey;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScanProjectConfigService {

    @Autowired
    ScanAssertService scanAssertService;

    @Autowired
    UserInputAssertion userInputAssertion;

    @Autowired
    ScanProjectConfigRepository repository;

    /**
     * Get configuration for project - and checks access
     * 
     * @param projectId
     * @param configId
     * @return project scan configuration or <code>null</code>
     */
    public ScanProjectConfig get(String projectId, ScanProjectConfigID configId) {
        return get(projectId, configId, true);
    }

    /**
     * Get configuration for project - and checks access if wanted. If there is no
     * configuration (null), a dedicated configuration will be created containing
     * default data
     * 
     * @param projectId
     * @param configId
     * @param checkAccess when <code>true</code> access is checked, otherwise not
     * @param defaultData initial data to set when configuration did not exist and
     *                    is created. Can be also <code>null</code>
     * @return persisted project scan configuration or a <code>new</code> scan
     *         configuration with default value
     */
    public ScanProjectConfig getOrCreate(String projectId, ScanProjectConfigID configId, boolean checkAccess, String defaultData) {
        ScanProjectConfig config = get(projectId, configId, checkAccess);
        if (config == null) {
            config = new ScanProjectConfig(configId, projectId);
            config.setData(defaultData);
        }
        return config;
    }

    /**
     * Get configuration for project - and checks access
     * 
     * @param projectId
     * @param configId
     * @param checkAccess when <code>true</code> access is checked, otherwise not
     * @return project scan configuration or <code>null</code>
     */
    public ScanProjectConfig get(String projectId, ScanProjectConfigID configId, boolean checkAccess) {
        userInputAssertion.isValidProjectId(projectId);
        if (checkAccess) {
            scanAssertService.assertUserHasAccessToProject(projectId);
        }
        ScanProjectConfigCompositeKey key = new ScanProjectConfigCompositeKey(configId, projectId);
        Optional<ScanProjectConfig> found = repository.findById(key);
        return found.orElse(null);
    }

    /**
     * Set configuration for project (means will persist given change to config)
     * 
     * @param projectId
     * @param configId
     * @param data      when <code>null</code> existing entry will be deleted on
     *                  database
     */
    public void set(String projectId, ScanProjectConfigID configId, String data) {
        userInputAssertion.isValidProjectId(projectId);
        scanAssertService.assertUserHasAccessToProject(projectId);

        ScanProjectConfigCompositeKey key = new ScanProjectConfigCompositeKey(configId, projectId);
        Optional<ScanProjectConfig> found = repository.findById(key);

        ScanProjectConfig config = found.orElse(null);

        boolean shallDelete = data == null;
        if (shallDelete) {
            if (config == null) {
                return;
            }
            repository.delete(config);
            return;
        } else {
            if (config == null) {
                config = new ScanProjectConfig(new ScanProjectConfigCompositeKey(configId, projectId));
            }
            config.setData(data);
            repository.save(config);
        }

    }

}
