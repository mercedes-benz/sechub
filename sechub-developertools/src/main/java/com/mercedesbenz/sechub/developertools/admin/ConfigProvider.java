// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin;

public interface ConfigProvider {

    public String getApiToken();

    public String getUser();

    public String getServer();

    public String getProtocol();

    public int getPort();

}
