// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

public interface SecHubFileSystemContainer {

    Optional<SecHubFileSystemConfiguration> getFileSystem();

}