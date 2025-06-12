// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.container;

public abstract class AbstractTestContainer {

    protected int port;
    protected String username;
    protected String password;

    protected BashScriptContainerLaunchConfig config;
    protected final BashScriptContainerLauncher launcher;
    protected final ContainerPathUtils pathUtils;

    public AbstractTestContainer(int port, String username, String password) {
        this.port = port;
        this.username = username;
        this.password = password;

        this.launcher = new BashScriptContainerLauncher();
        this.pathUtils = new ContainerPathUtils();
    }

    public abstract void start() throws Exception;

    public abstract void stop() throws Exception;

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
