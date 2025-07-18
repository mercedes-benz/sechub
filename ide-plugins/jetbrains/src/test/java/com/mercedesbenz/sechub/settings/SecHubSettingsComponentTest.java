package com.mercedesbenz.sechub.settings;

import org.junit.Test;

import static org.junit.Assert.*;

public class SecHubSettingsComponentTest {

    @Test
    public void userName() {
        /* prepare */
        SecHubSettingsComponent componentToTest = new SecHubSettingsComponent();
        componentToTest.setUserNameText("test");

        /* execute */
        String userNameText = componentToTest.getUserNameText();

        /* test */
        assertEquals("test", userNameText);
    }

    @Test
    public void secHubServerUrl() {
        /* prepare */
        SecHubSettingsComponent componentToTest = new SecHubSettingsComponent();
        componentToTest.setSecHubServerUrlText("http://example.com");

        /* execute */
        String secHubServerUrlText = componentToTest.getSecHubServerUrlText();

        /* test */
        assertEquals("http://example.com", secHubServerUrlText);
    }

    @Test
    public void apiTokenPassword() {
        /* prepare */
        SecHubSettingsComponent componentToTest = new SecHubSettingsComponent();
        componentToTest.setApiTokenPassword("secret");

        /* execute */
        String apiTokenPassword = componentToTest.getApiTokenPassword();

        /* test */
        assertEquals("secret", apiTokenPassword);
    }

    @Test
    public void useCustomWebUiUrl() {
        /* prepare */
        SecHubSettingsComponent componentToTest = new SecHubSettingsComponent();
        assertFalse(componentToTest.useCustomWebUiUrl());
        componentToTest.setUseCustomWebUiUrl(true);

        /* execute */
        boolean useCustomWebUiUrl = componentToTest.useCustomWebUiUrl();

        /* test */
        assertTrue(useCustomWebUiUrl);
    }

    @Test
    public void webUiUrlText_with_use_custom_web_ui_url_enabled() {
        /* prepare */
        SecHubSettingsComponent componentToTest = new SecHubSettingsComponent();
        componentToTest.setUseCustomWebUiUrl(true);
        componentToTest.setSecHubServerUrlText("http://example.com");
        componentToTest.setWebUiUrlText("http://webui.example.com");

        /* execute */
        String webUiUrlText = componentToTest.getWebUiUrlText();

        /* test */
        assertEquals("http://webui.example.com", webUiUrlText);
    }

    @Test
    public void webUiUrlText_with_use_custom_web_ui_url_disabled() {
        /* prepare */
        SecHubSettingsComponent componentToTest = new SecHubSettingsComponent();
        componentToTest.setUseCustomWebUiUrl(false);
        componentToTest.setSecHubServerUrlText("http://example.com");
        componentToTest.setWebUiUrlText("http://webui.example.com");

        /* execute */
        String webUiUrlText = componentToTest.getWebUiUrlText();

        /* test */
        assertEquals("http://example.com/login", webUiUrlText);
    }

    @Test
    public void sslTrustAll() {
        /* prepare */
        SecHubSettingsComponent componentToTest = new SecHubSettingsComponent();
        assertFalse(componentToTest.isSslTrustAll());
        componentToTest.setSslTrustAll(true);

        /* execute */
        boolean sslTrustAll = componentToTest.isSslTrustAll();

        /* test */
        assertTrue(sslTrustAll);
    }

}