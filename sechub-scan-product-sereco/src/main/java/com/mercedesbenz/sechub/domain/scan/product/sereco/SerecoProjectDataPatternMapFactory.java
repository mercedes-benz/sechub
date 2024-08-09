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
import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;

@Component
public class SerecoProjectDataPatternMapFactory {

    /**
     * Takes all the projectData entries where wildcards are allowed and create
     * compile regex patterns.
     *
     * @param falsePositives
     * @return unmodifiable map with projectData wildcard entries as key and the
     *         created regex pattern as value or an empty map if no projectData
     *         where found.
     */
    public Map<String, Pattern> create(List<FalsePositiveEntry> falsePositives) {
        notNull(falsePositives, " falsePositives may not be null");
        Map<String, Pattern> patternMap = new HashMap<>();
        for (FalsePositiveEntry falsePositiveEntry : falsePositives) {
            FalsePositiveProjectData projectData = falsePositiveEntry.getProjectData();
            if (projectData != null && projectData.getWebScan() != null) {
                patternMap.putAll(createMapFromProjectDataWebScan(projectData.getWebScan()));
            }
        }
        return Collections.unmodifiableMap(patternMap);
    }

    private Map<String, Pattern> createMapFromProjectDataWebScan(WebscanFalsePositiveProjectData webScan) {
        List<String> hostPatterns = webScan.getHostPatterns();
        List<String> urlPathPatterns = webScan.getUrlPathPatterns();
        notNull(hostPatterns, " hostPatterns may not be null");
        notNull(urlPathPatterns, " urlPathPatterns may not be null");

        int mapSize = hostPatterns.size() + urlPathPatterns.size();
        Map<String, Pattern> patternMap = new HashMap<>(mapSize);

        for (String hostPattern : hostPatterns) {
            Pattern compiledPattern = createCompiledPattern(hostPattern);
            patternMap.put(hostPattern, compiledPattern);
        }

        for (String urlPathPattern : urlPathPatterns) {
            Pattern compiledPattern = createCompiledPattern(urlPathPattern);
            patternMap.put(urlPathPattern, compiledPattern);
        }
        return patternMap;
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
