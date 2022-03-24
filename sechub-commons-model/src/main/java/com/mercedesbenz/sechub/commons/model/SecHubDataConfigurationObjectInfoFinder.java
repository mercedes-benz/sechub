package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class SecHubDataConfigurationObjectInfoFinder {
    
    
    private class xyz implements SecHubDataConfigurationObject{

        @Override
        public String getUniqueName() {
            return null;
        }
        
    }

    public List<SecHubDataConfigurationObjectInfo> findReferences(SecHubCodeScanConfiguration codeScanConfiguration, SecHubConfigurationModel model) {
        List<SecHubDataConfigurationObjectInfo> referenceList = new ArrayList<>();
        
        // code scan can itself be a configuration object
        Optional<SecHubFileSystemConfiguration> internalFileSystem = codeScanConfiguration.getFileSystem();
        if (internalFileSystem.isPresent()) {
            
            /* we createan arificial info object */
            SecHubDataConfigurationObject obj = new SecHubDataConfigurationObject() {
                
                @Override
                public String getUniqueName() {
                    // TODO Auto-generated method stub
                    return null;
                }
            };;
            
        }
        
        findReferencesForUsageByName(referenceList, codeScanConfiguration,model);
        return referenceList;
    }

    public List<SecHubDataConfigurationObjectInfo> findReferences(SecHubOpenAPIConfiguration openApiConfiguration, SecHubConfigurationModel model) {
        List<SecHubDataConfigurationObjectInfo> referenceList = new ArrayList<>();
        findReferencesForUsageByName(referenceList, openApiConfiguration,model);
        return referenceList;
    }

    private List<SecHubDataConfigurationObjectInfo> findReferencesForUsageByName(List<SecHubDataConfigurationObjectInfo> referenceList,
            SecHubDataConfigurationUsageByName openApiConfiguration, SecHubConfigurationModel model) {

        Optional<SecHubDataConfiguration> dataOpt = model.getData();
        if (!dataOpt.isPresent()) {
            return referenceList;
        }
        SecHubDataConfiguration data = dataOpt.get();
        Set<String> names = openApiConfiguration.getNamesOfUsedDataConfigurationObjects();
        for (String name : names) {

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
        return referenceList;
    }

    private void addReference(List<SecHubDataConfigurationObjectInfo> referenceList, SecHubDataConfigurationObject configurationObject,
            SecHubDataConfigurationType type) {
        SecHubDataConfigurationObjectInfo info = new SecHubDataConfigurationObjectInfo();
        info.type = type;
        info.dataConfigurationObject = configurationObject;

        referenceList.add(info);
    }
}
