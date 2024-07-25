// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.nio.file.Path;

public interface PrepareToolContext {

    String getLocation();

    Path getUploadDirectory();

    Path getToolDownloadDirectory();

}