// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

public interface PDSKey {

    /**
     * @return identifier, never <code>null</code>
     */
    String getId();

    /**
     *
     * @return <code>true</code> when this parameter is generated at runtime
     */
    boolean isGenerated();

    /**
     * @return <code>true</code> when mandatory
     */
    boolean isMandatory();

    /**
     *
     * @return a description about the key
     */
    String getDescription();

    /**
     *
     * @return <code>true</code> when this key is always sent to PDS
     */
    boolean isSentToPDS();

    /**
     * Recommend means, that it is a good option to set the default value
     *
     * @return <code>true</code> when recommended
     */
    boolean isDefaultRecommended();

    /**
     *
     * @return a default value
     */
    String getDefaultValue();

}