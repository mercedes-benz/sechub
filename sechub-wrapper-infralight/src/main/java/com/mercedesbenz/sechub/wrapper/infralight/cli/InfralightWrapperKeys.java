// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.infralight.cli;

import com.mercedesbenz.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.mercedesbenz.sechub.commons.pds.PDSSolutionVariableKey;
import com.mercedesbenz.sechub.commons.pds.PDSSolutionVariableType;

/**
 * An enumeration of the keys used by the Checkmarx wrapper. The default PDS
 * keys are not listed here. They can be found in
 * {@link PDSConfigDataKeyProvider} . All of the keys must be defined at the PDS
 * config file to be available as job parameters!
 *
 * @author Albert Tregnaghi
 *
 */
public enum InfralightWrapperKeys implements PDSSolutionVariableKey {
    /* @formatter:off */

    INFRALIGHT_MOCKING_ENABLED(
            InfralightWrapperKeyConstants.KEY_PDS_INFRALIGHT_MOCKING_ENABLED,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "When 'true' than, instead of a real infra light scan result check the mocked infralight scan folder will be inspected"
            + " is only necessary for tests."),


    /* @formatter:on */
    ;

    private String key;
    private PDSSolutionVariableType type;
    private String description;

    InfralightWrapperKeys(String key, PDSSolutionVariableType type, String description) {
        this.key = key;
        this.type = type;
        this.description = description;
    }

    @Override
    public String getVariableKey() {
        return key;
    }

    @Override
    public PDSSolutionVariableType getVariableType() {
        return type;
    }

    @Override
    public String getVariableDescription() {
        return description;
    }
}
