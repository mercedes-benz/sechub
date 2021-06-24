// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Profiles;

@Service
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestSecurityLogService extends DefaultSecurityLogService{
    
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
