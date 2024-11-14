// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.ConfigurationFailureException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData.PDSAssetData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetDetailData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFileData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetService;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

@Service
public class PDSTemplateMetaDataService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSTemplateMetaDataService.class);

    @Autowired
    AssetService assetService;

    @Autowired
    RelevantScanTemplateDefinitionFilter filter;

    public List<PDSTemplateMetaData> createTemplateMetaData(List<TemplateDefinition> templateDefinitions, String pdsProductId, ScanType scanType,
            SecHubConfigurationModel configuration) throws ConfigurationFailureException {

        List<PDSTemplateMetaData> result = new ArrayList<>();

        List<TemplateDefinition> filteredDefinitions = filter.filter(templateDefinitions, scanType, configuration);
        if (filteredDefinitions.isEmpty()) {
            LOG.debug("Given {} template definitions, after filtering: {}", templateDefinitions.size(), filteredDefinitions.size());
        }

        resolveAssetFileDataAndAddToResult(result, pdsProductId, filteredDefinitions);

        return result;
    }

    /**
     * Ensures that asset files in given PDS template meta data is available in
     * storage.
     *
     * @param metaDataList list containing template meta data
     * @throws ConfigurationFailureException
     */
    public void ensureTemplateAssetFilesAreAvailableInStorage(List<PDSTemplateMetaData> metaDataList) throws ConfigurationFailureException {
        for (PDSTemplateMetaData metaData : metaDataList) {

            PDSAssetData assetData = metaData.getAssetData();

            String assetId = assetData.getAsset();
            String fileName = assetData.getFile();

            assetService.ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase(fileName, assetId);

        }
    }

    private void resolveAssetFileDataAndAddToResult(List<PDSTemplateMetaData> result, String pdsProductId, List<TemplateDefinition> filteredDefinitions)
            throws ConfigurationFailureException {
        if (filteredDefinitions.isEmpty()) {
            return;
        }

        String filename = pdsProductId + ".zip";

        for (TemplateDefinition definition : filteredDefinitions) {
            String assetId = definition.getAssetId();
            AssetDetailData details = null;
            try {
                details = assetService.fetchAssetDetails(assetId);
            } catch (NotFoundException e) {
                /* asset does not exist */
                throw new ConfigurationFailureException("The asset " + assetId + " does not exist! Cannot transform", e);
            }

            List<AssetFileData> files = details.getFiles();
            boolean fileForTemplateFound = false;

            for (AssetFileData fileData : files) {
                if (!filename.equals(fileData.getFileName())) {
                    continue;
                }
                /* found */
                PDSAssetData assetData = new PDSAssetData();
                assetData.setAsset(assetId);
                assetData.setFile(fileData.getFileName());
                assetData.setChecksum(fileData.getChecksum());

                PDSTemplateMetaData metaData = new PDSTemplateMetaData();
                metaData.setTemplate(definition.getId());
                metaData.setType(definition.getType());
                metaData.setAssetData(assetData);

                result.add(metaData);

                fileForTemplateFound = true;
                break;
            }

            if (!fileForTemplateFound) {
                throw new ConfigurationFailureException("The asset " + assetId + " does not contain file '" + filename + "'");
            }
        }
    }
}
