// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.util.HashMap;
import java.util.Map;

import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.integrationtest.internal.TestRestHelper.RestHelperTarget;
import com.daimler.sechub.test.TestPortProvider;
import com.daimler.sechub.test.TestURLBuilder;

/**
 * Test context class. Contains initial data like port, hostname etc.
 *
 * @author Albert Tregnaghi
 *
 */
public class IntegrationTestContext {

    static IntegrationTestContext testContext = new IntegrationTestContext();

    private MockEmailAccess mailAccess = MockEmailAccess.mailAccess();

    private Map<TestUser, TestRestHelper> restHelperMap = new HashMap<>();
    private Map<TestUser, TestRestHelper> pdsRestHelperMap = new HashMap<>();
    private String hostname = "localhost";
    private int port = TestPortProvider.DEFAULT_INSTANCE.getIntegrationTestServerPort();
    private int pdsPort = TestPortProvider.DEFAULT_INSTANCE.getIntegrationTestPDSPort();

    private TestURLBuilder urlBuilder;

    private TestURLBuilder pdsUrlBuilder;

    private TestUser superAdminUser = TestAPI.SUPER_ADMIN;

    public static IntegrationTestContext get() {
        return testContext;
    }

    public void rebuild() {
        /* force recreation of builders */
        urlBuilder = null;
        pdsUrlBuilder = null;
        restHelperMap.clear();
    }

    public void setSuperAdminUser(TestUser superAdminUser) {
        this.superAdminUser = superAdminUser;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPdsPort(int pdsPort) {
        this.pdsPort = pdsPort;
    }

    public TestURLBuilder getUrlBuilder() {
        if (urlBuilder == null) {
            urlBuilder = new TestURLBuilder("https", port, hostname);
        }
        return urlBuilder;
    }

    public TestURLBuilder getPDSUrlBuilder() {
        if (pdsUrlBuilder == null) {
            pdsUrlBuilder = new TestURLBuilder("https", pdsPort, hostname);
        }
        return pdsUrlBuilder;
    }

    /**
     * @return template for super admin
     */
    public TestRestHelper getTemplateForSuperAdmin() {
        return getRestHelper(getSuperAdminUser());
    }

    private TestUser getSuperAdminUser() {
        return superAdminUser;
    }

    private IntegrationTestContext() {

    }

    public TestRestHelper getSuperAdminRestHelper() {
        return getRestHelper(getSuperAdminUser());
    }

    public TestRestHelper getRestHelper(TestUser user) {
        return restHelperMap.computeIfAbsent(user, this::createRestHelper);
    }

    public TestRestHelper getPDSRestHelper(TestUser user) {
        return pdsRestHelperMap.computeIfAbsent(user, this::createPDSRestHelper);
    }

    private TestRestHelper createRestHelper(TestUser user) {
        return new TestRestHelper(user, RestHelperTarget.SECHUB_SERVER);
    }

    private TestRestHelper createPDSRestHelper(TestUser user) {
        return new TestRestHelper(user, RestHelperTarget.SECHUB_PDS);
    }

    public MockEmailAccess emailAccess() {
        return mailAccess;
    }

}
