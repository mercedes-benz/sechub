// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import java.util.UUID;

import com.daimler.sechub.sharedkernel.TraceLogID;

public class SecHubJobTraceLogID extends TraceLogID<ScheduleSecHubJob> {

    public SecHubJobTraceLogID(ScheduleSecHubJob source) {
        super(source);
    }

    public static SecHubJobTraceLogID traceLogID(ScheduleSecHubJob job) {
        return new SecHubJobTraceLogID(job);
    }

    @Override
    protected String createContent(ScheduleSecHubJob job) throws Exception {
        if (job == null) {
            return null;
        }
        UUID uuid = job.getUUID();
        if (uuid == null) {
            return "<JOB has no UUID!>";
        }
        return uuid.toString();
    }

}
