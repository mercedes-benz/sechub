// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface ZapWrapperConfiguration {

    public String getTargetURL();

    public Path getReportFile();

    public String getJobUUID();

    public File getSecHubConfigFile();

    public boolean isAjaxSpiderEnabled();

    public String getAjaxSpiderBrowserId();

    public boolean isActiveScanEnabled();

    public String getZapHost();

    public int getZapPort();

    public String getZapApiKey();

    public boolean isVerboseEnabled();

    public String getProxyHost();

    public int getProxyPort();

    public String getProxyRealm();

    public String getProxyUsername();

    public String getProxyPassword();

    public List<String> getDeactivateRules();

    public boolean isConnectionCheckEnabled();

    public int getMaxNumberOfConnectionRetries();

    public int getRetryWaittimeInMilliseconds();

    public String getPDSUserMessageFolder();

    public String getPDSEventFolder();

    public String getGroovyLoginScriptFile();

    public String getPacFilePath();

    public boolean isNoHeadless();
}
