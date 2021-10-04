// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import java.util.ArrayList;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubMessage;
import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.commons.model.SecHubStatus;

/**
 * A component to merge different sechub report transformation results into one.
 * At the moment very dumb, by just adding all content.
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class ReportTransformationResultMerger {

    public ReportTransformationResult merge(ReportTransformationResult result1, ReportTransformationResult result2) {
        if (result1 == null) {
            return result2;
        }
        if (result2 == null) {
            return result1;
        }

        ReportTransformationResult mergedTransformerResult = new ReportTransformationResult();
        SecHubResult merged = mergedTransformerResult.getResult();

        mergeFindings(result1.getResult(), merged);
        mergeFindings(result2.getResult(), merged);

        mergeFalsePositives(result1.getResult(), merged);
        mergeFalsePositives(result2.getResult(), merged);

        mergeCounts(result1.getResult(), merged);
        mergeCounts(result2.getResult(), merged);

        mergeStatus(result1, mergedTransformerResult);
        mergeStatus(result2, mergedTransformerResult);

        mergeMessages(result1, mergedTransformerResult);
        mergeMessages(result2, mergedTransformerResult);
        
        return mergedTransformerResult;
    }

    private void mergeMessages(ReportTransformationResult result, ReportTransformationResult mergedTransformerResult) {
        Set<SecHubMessage> resultMessages = result.getMessages();
        for (SecHubMessage message: resultMessages) {
            mergedTransformerResult.getMessages().add(message);
        }
    }

    private void mergeStatus(ReportTransformationResult result, ReportTransformationResult merged) {
        /* when one transformation result has failed, we mark ALL (merged) as failed: */
        if (result.getStatus() == SecHubStatus.FAILED) {
            merged.setStatus(SecHubStatus.FAILED);
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
