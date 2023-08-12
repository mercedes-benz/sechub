// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Implementations of this interface are meant not to be automatically tested by
 * continuous integration builds or in any other "normal" test phase. They are
 * filtered/ignored per default.
 *
 * They shall be only executed by developers directly by setting the system
 * property {@value TestConstants#MANUAL_TEST_BY_DEVELOPER} to
 * <code>true</code>.
 *
 * There can be multiple reasons for such tests:
 * <ul>
 * <li>The test is only to generate something (in this case the implementation
 * name contains a hint about generation). In unit tests we have full access to
 * all classes of a sub project without having it inside the productive
 * code.</li>
 * <li>The test cannot be done automated because some special preparations are
 * necessary or the test makes only sense at development time.</li>
 * </ul>
 *
 * @author Albert Tregnaghi
 *
 */
@EnabledIfSystemProperty(named = TestConstants.MANUAL_TEST_BY_DEVELOPER, matches = "true", disabledReason = TestConstants.DESCRIPTION_DISABLED_BECAUSE_A_MANUAL_TEST)
public interface ManualTest {

}
