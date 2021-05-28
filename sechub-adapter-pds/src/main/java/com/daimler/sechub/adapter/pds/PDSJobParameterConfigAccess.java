// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.util.Collections;
import java.util.Map;

/**
 * Normally we do NOT want to give access to job parameters after executors have build the
 * configuration. But for some special reasons (PDS adapter is adding sechub storage path) 
 * we still need a modification. Most parts will need the unmodifiable wrapper. And the exceptions
 * are easy to identify by opening caller hierarchy of {@link #getModifiableJobParameters()}
 * @author Albert Tregnaghi
 *
 */
public class PDSJobParameterConfigAccess {

    private Map<String, String> modifaibleJobParameters;
    private Map<String, String> unmodifiableWrapper;

    public PDSJobParameterConfigAccess(Map<String, String> jobParameters) {
        this.modifaibleJobParameters=jobParameters;
        this.unmodifiableWrapper = Collections.unmodifiableMap(jobParameters);
    }

    public Map<String, String> getUnmodifiableJobParameters() {
        return unmodifiableWrapper;
    }
    
    public Map<String, String> getModifiableJobParameters() {
        return modifaibleJobParameters;
    }
}
