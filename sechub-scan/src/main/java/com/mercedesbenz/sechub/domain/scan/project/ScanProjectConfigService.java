// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig.ScanProjectConfigCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

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
        userInputAssertion.assertIsValidProjectId(projectId);
        if (checkAccess) {
            scanAssertService.assertUserHasAccessToProject(projectId);
        }
        ScanProjectConfigCompositeKey key = new ScanProjectConfigCompositeKey(configId, projectId);
        Optional<ScanProjectConfig> found = repository.findById(key);
        return found.orElse(null);
    }

    /**
     * Removes project configuration entry
     *
     * @param projectId project identifier
     * @param configId  configuration identifier
     */
    public void unset(String projectId, ScanProjectConfigID configId) {
        set(projectId, configId, null);
    }

    /**
     * Set configuration for project (means will persist given change to
     * configuration)
     *
     * @param projectId project identifier
     * @param configId  configuration identifier
     * @param data      when <code>null</code> existing entry will be deleted on
     *                  database
     */
    public void set(String projectId, ScanProjectConfigID configId, String data) {
        userInputAssertion.assertIsValidProjectId(projectId);
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

    @Transactional
    public void deleteAllConfigurationsOfGivenConfigIdsAndValue(Set<String> configIds, String value) {
        repository.deleteAllConfigurationsOfGivenConfigIdsAndValue(configIds, value);
    }

    public List<String> findAllData(ScanProjectConfigID scanConfigType) {
        return repository.findAllDataForConfigId(scanConfigType.getId());
    }

}
