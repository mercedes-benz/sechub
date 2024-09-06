// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.notNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveEntry;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;

@Component
public class SerecoProjectDataPatternMapFactory {

    /**
     * Takes all the projectData entries where wildcards are allowed and create
     * compile regex patterns.
     *
     * @param falsePositives
     * @return unmodifiable map with projectData ids as key and the regex pattern
     *         for the urlPattern as value or an empty map if no projectData where
     *         found.
     */
    public Map<String, Pattern> create(List<FalsePositiveEntry> falsePositives) {
        notNull(falsePositives, " falsePositives may not be null");

        Map<String, Pattern> patternMap = new HashMap<>();
        for (FalsePositiveEntry falsePositiveEntry : falsePositives) {
            FalsePositiveProjectData projectData = falsePositiveEntry.getProjectData();
            if (projectData != null && projectData.getWebScan() != null) {
                String id = projectData.getId();
                Pattern pattern = createCompiledPattern(projectData.getWebScan().getUrlPattern());
                patternMap.put(id, pattern);
            }
        }
        return Collections.unmodifiableMap(patternMap);
    }

    private Pattern createCompiledPattern(String regexString) {
        String escaped = Pattern.quote(regexString);
        String pattern = escaped.replace("*", "\\E.*\\Q");
        // remove unnecessary empty quotes to simplify the regex
        pattern = pattern.replace("\\Q\\E", "");
        // make sure the patterns matches the string from start to finish and not just a
        // substring in between
        pattern = "^" + pattern + "$";
        Pattern compiledPattern = Pattern.compile(pattern);
        return compiledPattern;
    }

}
