package com.mercedesbenz.sechub.sharedkernel.analytic;

public interface AnalyticDataPart {

    /**
     * Returns information about the product which was responsible for collecting
     * this analytic data part.
     *
     * @return product info, never <code>null</code>
     */
    public AnalyticProductInfo getProductInfo();
}
