// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.analytic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class CodeAnalyticData implements AnalyticDataPart {

    private Map<String, LanguageData> languages = new HashMap<>();

    private AnalyticProductInfo productInfo;

    public CodeAnalyticData() {
        productInfo = new AnalyticProductInfo();
    }

    public Set<String> getLanguages() {
        return languages.keySet();
    }

    public AnalyticProductInfo getProductInfo() {
        return productInfo;
    }

    public void setLinesOfCodeForLanguage(String language, long linesOfCode) {
        ensureLanguageData(language).linesOfCode = linesOfCode;
    }

    public void setFilesForLanguage(String language, long files) {
        ensureLanguageData(language).files = files;
    }

    public long getFilesForLanguage(String language) {
        return ensureLanguageData(language).files;
    }

    public long getLinesOfCodeForLanguage(String language) {
        return ensureLanguageData(language).linesOfCode;
    }

    public long calculateFilesForAllLanguages() {
        long files = 0;

        for (LanguageData data : languages.values()) {
            files += data.files;
        }

        return files;
    }

    public long calculateLinesOfCodeForAllLanguages() {
        long linesOfCode = 0;

        for (LanguageData data : languages.values()) {
            linesOfCode += data.linesOfCode;
        }

        return linesOfCode;
    }

    private LanguageData ensureLanguageData(String language) {
        String normalizedLanguageName = normalizeLanguage(language);
        LanguageData languageData = this.languages.computeIfAbsent(normalizedLanguageName, (n) -> new LanguageData());
        return languageData;
    }

    private String normalizeLanguage(String language) {
        if (language == null) {
            return "";
        }
        // simple normalization
        return language.trim().toLowerCase();
    }

    @JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    private static class LanguageData {
        long files;
        long linesOfCode;
    }

}
