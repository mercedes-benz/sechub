package com.mercedesbenz.sechub.sharedkernel.analytic;

import java.util.LinkedHashSet;
import java.util.Set;

public class CodeAnalyticData implements AnalyticProductResult {

    private Set<String> languages = new LinkedHashSet<>();
    private AnalyticProductData productData;

    public CodeAnalyticData() {
        productData = new AnalyticProductData();
    }


    public Set<String> getLanguages() {
        return languages;
    }


    public AnalyticProductData getProductData() {
        return productData;
    }

    public void setLines(String language, long files) {

    }

    public void setFiles(String language, long files) {

    }

    public long getLinesOfCode() {
        return 1;
    }
    
    public long getLinesOfCodeForLanguage(String language) {
        return 0L;
    }

    public long getAmountOfFiles() {
        return 0L;
    }
    
    public long getAmountFilesForLanguage(String language) {
        return 0;
    }

}
