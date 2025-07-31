// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.settings;

import com.intellij.credentialStore.Credentials;
import org.junit.Test;
import javax.swing.*;

import static org.junit.Assert.*;

public class SecHubSettingsConfigurableTest {

    @Test
    public void displayName_is_SecHub() {
        /* prepare */
        SechubSettingsConfigurable configurable = new SechubSettingsConfigurable();

        /* execute + test */
        assertEquals("SecHub", configurable.getDisplayName());
    }

    @Test
    public void createComponent_returns_JComponent() {
        /* prepare */
        SechubSettingsConfigurable configurable = new SechubSettingsConfigurable();

        /* execute */
        JComponent component = configurable.createComponent();

        /* test */
        assertNotNull(component);
    }
}