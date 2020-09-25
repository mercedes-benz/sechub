// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubResult;

/**
 * A component to merge different sechub results into one. At the moment very dumb, by just adding all
 * content. 
 * @author Albert Tregnaghi
 *
 */
@Component
public class SecHubResultMerger {

    public SecHubResult merge(SecHubResult result1, SecHubResult result2) {
        if (result1==null) {
            return result2;
        }
        if (result2==null) {
            return result1;
        }
        
        SecHubResult merged = new SecHubResult();
        mergeFindings(result1, merged);
        mergeFindings(result2, merged);

        mergeFalsePositives(result1, merged);
        mergeFalsePositives(result2, merged);
        
        merged.setCount(result1.getCount()+result2.getCount());
        
        return merged;
    }

    private void mergeFindings(SecHubResult origin, SecHubResult merged) {
        merged.getFindings().addAll(origin.getFindings());
    }

    private void mergeFalsePositives(SecHubResult origin, SecHubResult merged) {
        if (origin.getFalsePositives()!=null) {
            if (merged.getFalsePositives()==null) {
                merged.setFalsePositives(new ArrayList<SecHubFinding>());
            }
            merged.getFalsePositives().addAll(origin.getFalsePositives());
        }
    }
}
