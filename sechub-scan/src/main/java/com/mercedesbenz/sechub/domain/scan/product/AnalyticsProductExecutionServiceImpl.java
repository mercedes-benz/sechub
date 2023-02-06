// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.analytic.AnalyticDataImportService;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.messaging.AnalyticMessageData;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

/**
 * This service executes all registered product executors having scan type
 * {@link ScanType#ANALYTICS}
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class AnalyticsProductExecutionServiceImpl extends AbstractProductExecutionService implements AnalyticsProductExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsProductExecutionServiceImpl.class);

    @Autowired
    AnalyticDataImportService analyticDataImportService;

    @Lazy
    @Autowired
    DomainMessageService domainMessageService;
    
    @Override
    protected void afterProductResultsStored(List<ProductResult> productResults, SecHubExecutionContext context) {
        LOG.debug("{} analytics product results stored.", productResults.size());

        /* import product results into analytic data model */
        AnalyticData analyticData = context.getAnalyticData();

        for (ProductResult productResult : productResults) {
            String analyticDataAsString = productResult.getResult();

            analyticDataImportService.importAnalyticDataParts(analyticDataAsString, analyticData);
        }

        sendAnalyticDataAvailableEvent(analyticData, context);

    }

    private void sendAnalyticDataAvailableEvent(AnalyticData analyticDataModel, SecHubExecutionContext context) {
        DomainMessage domainMessage = new DomainMessage(MessageID.ANALYZE_SCAN_RESULTS_AVAILABLE);

        AnalyticMessageData analyticDataMessage = new AnalyticMessageData();
        analyticDataMessage.setAnalyticData(analyticDataModel);

        domainMessage.set(MessageDataKeys.ANALYTIC_SCAN_RESULT_DATA, analyticDataMessage);
        domainMessage.set(MessageDataKeys.SECHUB_EXECUTION_UUID, context.getExecutionUUID());
        domainMessageService.sendAsynchron(domainMessage);
    }

    public boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {

        Optional<SecHubCodeScanConfiguration> codeScanOption = configuration.getCodeScan();
        Optional<SecHubLicenseScanConfiguration> licenseScan = configuration.getLicenseScan();

        if (codeScanOption.isPresent() || licenseScan.isPresent()) {
            return true;
        }
        return false;
    }

}
