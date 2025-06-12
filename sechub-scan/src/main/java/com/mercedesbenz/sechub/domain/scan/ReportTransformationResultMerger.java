// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.ArrayList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;

/**
 * A component to merge different SecHub report transformation results into one.
 * At the moment very dumb, by just adding all content.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class ReportTransformationResultMerger {

    private static final Logger LOG = LoggerFactory.getLogger(ReportTransformationResultMerger.class);

    public ReportTransformationResult merge(ReportTransformationResult result1, ReportTransformationResult result2) {
        if (result1 == null) {
            return result2;
        }
        if (result2 == null) {
            return result1;
        }

        ReportTransformationResult mergedTransformerResult = new ReportTransformationResult();
        SecHubResult merged = mergedTransformerResult.getModel().getResult();

        mergeFindings(result1.getModel().getResult(), merged);
        mergeFindings(result2.getModel().getResult(), merged);

        mergeFalsePositives(result1.getModel().getResult(), merged);
        mergeFalsePositives(result2.getModel().getResult(), merged);

        mergeCounts(result1.getModel().getResult(), merged);
        mergeCounts(result2.getModel().getResult(), merged);

        mergeStatus(result1, mergedTransformerResult);
        mergeStatus(result2, mergedTransformerResult);

        mergeMessages(result1, mergedTransformerResult);
        mergeMessages(result2, mergedTransformerResult);

        mergeVersion(result1, mergedTransformerResult);
        mergeVersion(result2, mergedTransformerResult);

        return mergedTransformerResult;
    }

    private void mergeVersion(ReportTransformationResult result, ReportTransformationResult mergedTransformerResult) {
        /*
         * currently very simple approach: last one wins, changes by different versions
         * produces at least a WARNING log entry
         */
        String version = result.getModel().getReportVersion();
        String formerMergedVersion = mergedTransformerResult.getModel().getReportVersion();

        /* check there is another version already set by another product */
        if (formerMergedVersion == null) {
            if (version == null || version.isEmpty()) {
                LOG.debug("Transformation result has no version set - no existing one found");
                return;
            }
            mergedTransformerResult.getModel().setReportVersion(version);
        } else {

            if (formerMergedVersion.equals(version)) {
                /* same - so just ignore */
                return;

            }
            if (version == null || version.isEmpty()) {
                LOG.debug("Transformation result has no version set - so keep existing one:{}", formerMergedVersion);
            } else {
                mergedTransformerResult.getModel().setReportVersion(version);
                LOG.warn("Different report version found! Transformation result has version {} - will replace in merge result (former report version :{})",
                        version, formerMergedVersion);
            }
        }

    }

    private void mergeMessages(ReportTransformationResult result, ReportTransformationResult mergedTransformerResult) {
        Set<SecHubMessage> resultMessages = result.getModel().getMessages();
        for (SecHubMessage message : resultMessages) {
            mergedTransformerResult.getModel().getMessages().add(message);
        }
    }

    private void mergeStatus(ReportTransformationResult result, ReportTransformationResult merged) {
        /* when one transformation result has failed, we mark ALL (merged) as failed: */
        if (result.getModel().getStatus() == SecHubStatus.FAILED) {
            merged.getModel().setStatus(SecHubStatus.FAILED);
        }
    }

    private void mergeCounts(SecHubResult sechubResult, SecHubResult merged) {
        merged.setCount(merged.getCount() + sechubResult.getCount());
    }

    private void mergeFindings(SecHubResult origin, SecHubResult merged) {
        merged.getFindings().addAll(origin.getFindings());
    }

    private void mergeFalsePositives(SecHubResult origin, SecHubResult merged) {
        if (origin.getFalsePositives() != null) {
            if (merged.getFalsePositives() == null) {
                merged.setFalsePositives(new ArrayList<SecHubFinding>());
            }
            merged.getFalsePositives().addAll(origin.getFalsePositives());
        }
    }
}
