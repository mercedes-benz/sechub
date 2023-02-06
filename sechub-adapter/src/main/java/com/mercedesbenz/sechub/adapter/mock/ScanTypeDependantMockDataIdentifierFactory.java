// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.mock;

import static java.util.Objects.*;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;

public class ScanTypeDependantMockDataIdentifierFactory implements MockDataIdentifierFactory {

    @Autowired
    CodeScanPathCollector codeScanPathCollector;

    @Override
    public String createMockDataIdentifier(ScanType scanType, SecHubConfigurationModel configuration) {
        requireNonNull(scanType, "scantype may not be null!");
        requireNonNull(configuration, "configuration may not be null!");
        switch (scanType) {
        case LICENSE_SCAN:
        case CODE_SCAN:
            return createCollectedCodeScanPathAsIdentifier(configuration);
        case INFRA_SCAN:
            return fetchFirstInfraScanURLasIdentifier(configuration);
        case WEB_SCAN:
            return fetchWebScanURLasIdentifier(configuration);
        case REPORT:
        case UNKNOWN:
        default:
            return null;

        }
    }

    private String fetchFirstInfraScanURLasIdentifier(SecHubConfigurationModel configuration) {
        Optional<SecHubInfrastructureScanConfiguration> infraScanOpt = configuration.getInfraScan();
        if (!infraScanOpt.isPresent()) {
            return null;
        }
        SecHubInfrastructureScanConfiguration infraScan = infraScanOpt.get();
        List<URI> targetURIs = infraScan.getUris();
        if (targetURIs == null || targetURIs.isEmpty()) {
            return null;
        }
        return targetURIs.iterator().next().toString();
    }

    private String fetchWebScanURLasIdentifier(SecHubConfigurationModel configuration) {
        Optional<SecHubWebScanConfiguration> webScanOpt = configuration.getWebScan();
        if (!webScanOpt.isPresent()) {
            return null;
        }
        SecHubWebScanConfiguration webScan = webScanOpt.get();
        URI uri = webScan.getUrl();
        return Objects.toString(uri);
    }

    private String createCollectedCodeScanPathAsIdentifier(SecHubConfigurationModel configuration) {
        Set<String> sourceFolders = codeScanPathCollector.collectAllCodeScanPathes(configuration);
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> it = sourceFolders.iterator(); it.hasNext();) {
            String folder = it.next();
            if (folder == null) {
                continue;
            }
            sb.append(folder);
            if (it.hasNext()) {
                sb.append(';');
            }
        }

        return sb.toString();
    }

}
