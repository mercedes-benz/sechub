// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

/**<h3>Contract</h3>
 * Implementations must initialize only <b>ONE TIME</b> 
 * and will not destroy data after tests are done. So test execution time is faster.
 * <br><br>
 * <b>Tests may NOT change scenario test data!</b>
 * <h3>Details</h3> 
 * Similar to a normal scenario, but being <b>STATIC</b>!<br><br>
 * Normally, test data is dynamically created and destroyed in test method life cycle for a 
 * test scenario.<br><br> 
 * But this is can make tests very slow. Some times it is not necessary to destroy
 * test data. So if tests do not change test data (users, projects, relations) but only execute 
 * Jobs etc. a static test scenario makes sense. <br><br>
 * 
 * @author Albert Tregnaghi
 *
 */
public interface StaticTestScenario extends TestScenario {
    
    boolean isInitializationNecessary();
    
    default boolean isEmailResetNecessary() {
        return true;
    }

    default boolean isEventResetNecessary() {
        return true;
    }
   
}
