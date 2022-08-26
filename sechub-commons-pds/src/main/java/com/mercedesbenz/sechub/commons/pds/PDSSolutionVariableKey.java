// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

/**
 * An interface to identify PDS solution variables, means variables which are
 * used at PDS solution side and cane be changed by administrators either via
 * job parameter or by setting an environment entry at startup time of PDS.<br>
 * <br>
 * The implementations can be used by documentation generators and also to find
 * PDS solution variables inside source code.
 *
 * @author Albert Tregnaghi
 *
 */
public interface PDSSolutionVariableKey {

    /**
     * Returns the variable key which is always in a form like "a.b.c" even when
     * variable is of type {@link PDSSolutionVariableType#OPTIONAL_JOB_PARAMETER}
     *
     * @return the key
     */
    public String getVariableKey();

    public PDSSolutionVariableType getVariableType();

    /**
     * @return a short description about the variable
     */
    public String getVariableDescription();
}
