// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import com.daimler.sechub.integrationtest.api.TestProject;

/**
 * Those scenarios will NOT cleanup their data!
 * @author Albert Tregnaghi
 *
 */
public interface GrowingScenario extends TestScenario {

    /**
     * Create a growing identifier (e.g "01" , "02", ...) which will be returned by {@link #getGrowId()}
     */
    public void grow();
    
    /**
     * Main identifier used for creating name - may be only max 3 characters - e.g. s01 for scenario1.
     * Reason: Otherwise our generated user names and project names become too big and are not 
     * accepted by server. E.g. "s01_0009_user1"... 
     * @return main identifier  
     */
    public String getPrefixMainId();
    
    /**
     * Grow identifier used for creating name after main id - may be from 0000-9999 -  E.g. "s01_0009_user1"... 
     * @return grow identifier
     */
    public String getGrowId();
    
    public default String getName() {
        return getPrefixMainId()+"_"+getGrowId();
    }
    
    default TestProject newTestProject() {
        throw new IllegalStateException("A static test scenario does not have temp projects! static means 'does not change' ...");
    }
    
}
