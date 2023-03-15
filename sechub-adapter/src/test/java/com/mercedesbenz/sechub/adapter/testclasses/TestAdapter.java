// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.testclasses;

import com.mercedesbenz.sechub.adapter.AbstractAdapter;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;

public class TestAdapter extends AbstractAdapter<TestAdapterContextInterface, TestAdapterConfigInterface> implements TestAdapterInterface {

    String apiPrefix;

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    @Override
    protected String getAPIPrefix() {
        return apiPrefix;
    }

    @Override
    public AdapterExecutionResult execute(TestAdapterConfigInterface config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        return new AdapterExecutionResult(null);
    }

    @Override
    public int getAdapterVersion() {
        return 1;
    }

}
