package com.mercedesbenz.sechub.pds;

import java.util.Optional;
import java.util.Set;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationUsageByName;
import com.mercedesbenz.sechub.commons.model.SecHubOpenAPIConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.pds.config.PDSScanType;

public class ScanTypeBasedFilterDataBuilder {
    /*
     * FIXME Albert Tregnaghi, 2022-05-06: move the logic to sechub commons later.
     * The model validator should check the logic before sending to PDS! Also PDS
     * scan type could be removed, we have now access to sechub commons model
     */


    private ReferenceNameAndRootFolderArchiveFilterData data = new ReferenceNameAndRootFolderArchiveFilterData();
    
    private ScanTypeBasedFilterDataBuilder() {
        
    }
    
    public static ReferenceNameAndRootFolderArchiveFilterData create(PDSScanType type, SecHubConfigurationModel model) {
        ScanTypeBasedFilterDataBuilder builder = new ScanTypeBasedFilterDataBuilder();
        builder.createInternally(type, model);
        return builder.data;
    }
    
    private void createInternally(PDSScanType type, SecHubConfigurationModel model) {

        switch (type) {
        case CODE_SCAN:
            data.rootFolderAccepted = true;
            addAllUsages(model.getCodeScan(), false);
            break;
        case INFRA_SCAN:
            break;
        case LICENSE_SCAN:
            addAllUsages(model.getLicenseScan(), true);
            break;
        case REPORT:
            break;
        case UNKNOWN:
            break;
        case WEB_SCAN:
            Optional<SecHubWebScanConfiguration> webScanOpt = model.getWebScan();
            if (!webScanOpt.isPresent()) {
                throw new IllegalStateException("No webscan present but it is a " + type);
            }
            SecHubWebScanConfiguration webScan = webScanOpt.get();
            Optional<SecHubOpenAPIConfiguration> apiOpt = webScan.getOpenApi();
            addAllUsages(apiOpt, false);
            break;
        default:
            break;

        }
    }

    private void addAllUsages(Optional<? extends SecHubDataConfigurationUsageByName> usage, boolean mustHave) {
        if (!usage.isPresent()) {
            if (mustHave) {
                fail("Not contained");
            }
            return;
        }
        assertNotEmptyAndAddAll(usage.get());
    }

    private void assertNotEmptyAndAddAll(SecHubDataConfigurationUsageByName dataByName) {
        Set<String> names = dataByName.getNamesOfUsedDataConfigurationObjects();
        if (names.isEmpty()) {
            fail("may not be empty!");
        }
        data.acceptedReferenceNames.addAll(names);
    }

    private void fail(String message) {
        throw new IllegalStateException(message);
    }

}
