// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.List;
import java.util.Optional;

public interface SecHubFileSystemContainer {

    Optional<SecHubFileSystemConfiguration> getFileSystem();

    public List<String> getIncludes();

    public List<String> getExcludes();
}