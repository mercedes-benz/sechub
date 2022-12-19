// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;

/**
 * Those scenarios will NOT cleanup their data. Instead the projects, users etc.
 * have always different ids: At scenario preparation time the {@link #grow()}
 * method is called which will increase an internal `growId` which is part of
 * id.<br>
 * Inside the tests always the same constants are used (e.g.
 * <code>USER_1</code>) but internally the identifier for database calls etc.
 * does change.<br>
 * <br>
 * Because there is no need to cleanup old data, these tests have a faster
 * initialization and it is possible to execute them even parallel.<br>
 * <br>
 * For an scenario overview look at {@link IntegrationTestDataOverview
 * Overview}.
 *
 * @author Albert Tregnaghi
 *
 */
public interface GrowingScenario extends TestScenario {

    /**
     * Create a growing identifier (e.g "01" , "02", ...) which will be returned by
     * {@link #getGrowId()}
     */
    public void grow();

    /**
     * Main identifier used for creating name - may be only max 3 characters - e.g.
     * s01 for scenario1. Reason: Otherwise our generated user names and project
     * names become too big and are not accepted by server. E.g. "s01_0009_user1"...
     *
     * @return main identifier
     */
    public String getPrefixMainId();

    /**
     * Grow identifier used for creating name after main id - may be from 0000-9999
     * - E.g. "s01_0009_user1"...
     *
     * @return grow identifier
     */
    public String getGrowId();

    public default String getName() {
        return getPrefixMainId() + "_" + getGrowId();
    }

    default TestProject newTestProject() {
        throw new IllegalStateException("A static test scenario does not have temp projects! static means 'does not change' ...");
    }

}
