// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class SecHubDataConfigurationObjectInfoFinder {

    public List<SecHubDataConfigurationObjectInfo> findDataObjectsByName(SecHubConfigurationModel model, Set<String> names) {
        List<SecHubDataConfigurationObjectInfo> referenceList = new ArrayList<>();
        if (model==null || names==null || names.isEmpty()) {
            return referenceList;
        }
        Optional<SecHubDataConfiguration> dataOpt = model.getData();
        if (!dataOpt.isPresent()) {
            return referenceList;
        }
        SecHubDataConfiguration data = dataOpt.get();
        for (String name : names) {
            addReferenceForName(referenceList, data, name);
        }
        return referenceList;
    }

    private void addReferenceForName(List<SecHubDataConfigurationObjectInfo> referenceList, SecHubDataConfiguration data, String name) {
        for (SecHubBinaryDataConfiguration binary : data.getBinaries()) {
            String binaryName = binary.getUniqueName();
            if (Objects.equals(binaryName, name)) {
                addReference(referenceList, binary, SecHubDataConfigurationType.BINARY);
            }

        }
        for (SecHubSourceDataConfiguration binary : data.getSources()) {
            String binaryName = binary.getUniqueName();
            if (Objects.equals(binaryName, name)) {
                addReference(referenceList, binary, SecHubDataConfigurationType.SOURCE);
            }
        }
    }

    private void addReference(List<SecHubDataConfigurationObjectInfo> referenceList, SecHubDataConfigurationObject configurationObject,
            SecHubDataConfigurationType type) {
        SecHubDataConfigurationObjectInfo info = new SecHubDataConfigurationObjectInfo();
        info.type = type;
        info.dataConfigurationObject = configurationObject;

        referenceList.add(info);
    }

}
