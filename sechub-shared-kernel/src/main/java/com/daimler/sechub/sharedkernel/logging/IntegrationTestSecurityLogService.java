// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Profiles;

/**
 * Nearly same as default service implementation, but does remember all security
 * log data objects and has a getter method to obtain the values.
 * 
 * @author Albert Tregnaghi
 *
 */
@Service
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestSecurityLogService extends DefaultSecurityLogService {

    private List<SecurityLogData> logData = new ArrayList<>();

    @Override
    void doLogging(SecurityLogData data) {
        logData.add(data);

        super.doLogging(data);
    }

    public List<SecurityLogData> getLogData() {
        return logData;
    }

}
