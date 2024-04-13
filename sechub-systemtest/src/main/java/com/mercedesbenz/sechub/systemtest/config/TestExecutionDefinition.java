// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.Optional;

public class TestExecutionDefinition extends AbstractDefinition {

    private Optional<RunSecHubJobDefinition> runSecHubJob = Optional.ofNullable(null);

    public void setRunSecHubJob(Optional<RunSecHubJobDefinition> runSecHubJob) {
        this.runSecHubJob = runSecHubJob;
    }

    public Optional<RunSecHubJobDefinition> getRunSecHubJob() {
        return runSecHubJob;
    }

}
