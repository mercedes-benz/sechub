// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.cluster;

import org.springframework.stereotype.Service;

@Service
public class ClusterEnvironmentService {

    private ClusterEnvironment environment;

    /**
     * @return an environment instance
     */
    public ClusterEnvironment getEnvironment() {
        if (environment == null) {
            environment = new ClusterEnvironment();
        }
        return environment;
    }

}
