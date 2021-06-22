// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Profiles;

@Service
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestSecurityLogService extends SecurityLogService{
    
    private List<IntegrationTestSecurityLogEntry> logEntries = new ArrayList<>();
    
	public void log(SecurityLogType type, String message, Object ...objects ) {
	    IntegrationTestSecurityLogEntry entry = new IntegrationTestSecurityLogEntry();
	    entry.message=message;
	    entry.type=type;
	    entry.objects.addAll(Arrays.asList(objects));
	    
	    logEntries.add(entry);
	    
	    super.log(type, message, objects);
	}
	
	public List<IntegrationTestSecurityLogEntry> getLogEntries() {
        return logEntries;
    }

}
