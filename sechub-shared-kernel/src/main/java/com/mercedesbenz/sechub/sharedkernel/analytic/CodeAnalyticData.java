package com.mercedesbenz.sechub.sharedkernel.analytic;

import java.util.LinkedHashSet;
import java.util.Set;

public class CodeAnalyticData implements AnalyticProductResult {

    private Set<String> languages = new LinkedHashSet<>();
    private AnalyticProductData productData;

    public CodeAnalyticData() {
        productData = new AnalyticProductData();
    }

    public long getAmountOfFiles() {
        return 0L;
    }

    public Set<String> getLanguages() {
        return languages;
    }

    public long getLines(String language) {
        return 0L;
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

}
