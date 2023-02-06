// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mercedesbenz.sechub.domain.scan.product.CanceableProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;

public class SecHubExecutionHistory {

    private List<SecHubExecutionHistoryElement> elements;
    private List<SecHubExecutionHistoryElement> cachedCanceableProductExecutorElements;

    SecHubExecutionHistory() {
        elements = new ArrayList<>();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public List<SecHubExecutionHistoryElement> getAllElementsWithCanceableProductExecutors() {
        if (cachedCanceableProductExecutorElements == null) {

            List<SecHubExecutionHistoryElement> list = new ArrayList<>();
            for (SecHubExecutionHistoryElement historyElement : getUnmodifiableElements()) {
                ProductExecutor executor = historyElement.getProductExecutor();
                if (executor instanceof CanceableProductExecutor) {
                    list.add(historyElement);
                }
            }
            cachedCanceableProductExecutorElements = list;
        }
        return cachedCanceableProductExecutorElements;
    }

    public SecHubExecutionHistoryElement remember(ProductExecutor productExecutor, ProductExecutorData data) {
        resetCache();
        SecHubExecutionHistoryElement historyElement = new SecHubExecutionHistoryElement();
        historyElement.setProductExecutor(productExecutor);
        historyElement.setProductExecutorData(data);

        elements.add(historyElement);

        return historyElement;

    }

    public void forget(SecHubExecutionHistoryElement historyElement) {
        resetCache();
        elements.remove(historyElement);
    }

    List<SecHubExecutionHistoryElement> getUnmodifiableElements() {
        return Collections.unmodifiableList(elements);
    }

    private void resetCache() {
        cachedCanceableProductExecutorElements = null;
    }

}
