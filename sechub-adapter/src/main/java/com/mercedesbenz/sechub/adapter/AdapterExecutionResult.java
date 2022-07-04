package com.mercedesbenz.sechub.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;

public class AdapterExecutionResult {

    private String productResult;

    private List<SecHubMessage> productMessages = new ArrayList<>();

    public AdapterExecutionResult(String productResult) {
        this(productResult, null);
    }

    public AdapterExecutionResult(String productResult, Collection<SecHubMessage> messages) {
        this.productResult = productResult;
        if (messages != null) {
            this.productMessages.addAll(messages);
        }
    }

    /**
     * Returns productMessages from product
     *
     * @return list of product productMessages (unmodifiable), never
     *         <code>null</code>
     */
    public List<SecHubMessage> getProductMessages() {
        return Collections.unmodifiableList(productMessages);
    }

    /**
     * Returns the reporting result returned by product
     *
     * @return product result as string
     */
    public String getProductResult() {
        return productResult;
    }
}
