// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PDSExecutionCallableFactory {

    @Autowired
    PDSExecutionCallableServiceCollection serviceCollection;

    public PDSExecutionCallable createCallable(UUID jobUUID) {
        return new PDSExecutionCallable(jobUUID, serviceCollection);
    }
}
