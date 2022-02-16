// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.netsparker;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.domain.scan.OneInstallSetup;

public class NetsparkerInstallSetupImplTest {

    private NetsparkerInstallSetupImpl setupToTest;

    @Before
    public void before() {
        setupToTest = new NetsparkerInstallSetupImpl();
    }

    @Test
    public void is_extending_double_install() {
        assertTrue(setupToTest instanceof OneInstallSetup);
    }

}
