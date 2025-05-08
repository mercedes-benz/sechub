// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static com.mercedesbenz.sechub.sharedkernel.ProductIdentifier.*;
import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.report.ReportProductResultTransformer;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

@Service
/**
 * This service will load the product result data from report collectors and
 * transform those data a sechub result
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubReportProductTransformerService {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubReportProductTransformerService.class);

    @Autowired
    ProductResultRepository productResultRepository;

    @Autowired
    ReportTransformationResultMerger resultMerger;

    @Autowired
    List<ReportProductResultTransformer> transformers;

    /**
     * Will fetch output from report products for wanted sechub job and returns a
     * new created result.
     *
     * @param context
     * @return result never <code>null</code>
     */
    public ReportTransformationResult createResult(SecHubExecutionContext context) throws SecHubExecutionException {
        notNull(context, "Context may not be null!");
        notNull(context.getConfiguration(), "SecHubConfiguration may not be null!");

        UUID secHubJobUUID = context.getSechubJobUUID();
        ReportTransformationResult result = createResult(secHubJobUUID);

        addMetaDataToReport(context, result);
        return result;
    }

    private void addMetaDataToReport(SecHubExecutionContext context, ReportTransformationResult result) {
        SecHubReportMetaData reportMetaData = new SecHubReportMetaData();
        result.setMetaData(reportMetaData);
        
        reportMetaData.getExecuted().addAll(context.getUsedPublicScanTypes());
        
        addConfigMetaDataToReport(context, reportMetaData);
    }

    private void addConfigMetaDataToReport(SecHubExecutionContext context, SecHubReportMetaData reportMetaData) {
        SecHubConfiguration configuration = context.getConfiguration();
        Optional<SecHubConfigurationMetaData> userDefinedConfigMetaDataOpt = configuration.getMetaData();
        if (userDefinedConfigMetaDataOpt.isEmpty()) {
            return;
        }

        /*
         * we add desired meta data from configuration (user defined) back into the scan
         * result
         */
        SecHubConfigurationMetaData userDefinedConfigMetaData = userDefinedConfigMetaDataOpt.get();
        copyLabelsFromConfigToReport(userDefinedConfigMetaData, reportMetaData);
    }

    private void copyLabelsFromConfigToReport(SecHubConfigurationMetaData metaData, SecHubReportMetaData reportMetaData) {
        Map<String, String> userDefinedLabels = metaData.getLabels();
        reportMetaData.getLabels().putAll(userDefinedLabels);
    }

    ReportTransformationResult createResult(UUID secHubJobUUID) throws SecHubExecutionException {
        notNull(secHubJobUUID, "secHubJobUUID may not be null!");
        List<ProductResult> reportProductResults = productResultRepository.findAllProductResults(secHubJobUUID, SERECO);

        if (reportProductResults.isEmpty()) {
            throw new SecHubExecutionException("No report result found for:" + secHubJobUUID);
        }

        int reportProductResultAmount = reportProductResults.size();
        if (reportProductResultAmount > 1) {
            LOG.warn("Found {} report product results, should normally be only one!", reportProductResultAmount);
        }
        ReportTransformationResult transformResult = null;
        for (ProductResult reportProductResult : reportProductResults) {

            for (ReportProductResultTransformer transformer : transformers) {

                if (transformer.canTransform(reportProductResult.getProductIdentifier())) {
                    LOG.debug("Transformer {} is used to transform result", transformer.getClass().getSimpleName());
                    ReportTransformationResult transformedResult = transformer.transform(reportProductResult);
                    transformResult = resultMerger.merge(transformResult, transformedResult);
                }
            }

        }

        if (transformResult == null) {
            throw new SecHubExecutionException("No transformable report result format found for:" + secHubJobUUID);
        }
        return transformResult;
    }
}
