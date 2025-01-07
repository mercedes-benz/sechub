// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import static java.util.Objects.*;

public abstract class AbstractPDSKey<T extends PDSKey> implements PDSKey {

    private String id;
    private String description;
    private boolean mandatory;
    private boolean generated;
    private boolean sentToPDS;
    private String defaultValue;
    private boolean defaultRecommended;
    private boolean onlyForTesting;

    /**
     * Creates a new config data key
     *
     * @param id          identifier may never be <code>null</code>
     * @param description
     */
    public AbstractPDSKey(String id, String description) {
        requireNonNull(id, "Configuration data key identifier may not be null!");

        this.id = id.toLowerCase();
        this.description = description;
    }

    /**
     * @return identifier, never <code>null</code>
     */
    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isGenerated() {
        return generated;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isSentToPDS() {
        return sentToPDS;
    }

    @SuppressWarnings("unchecked")
    public T markMandatory() {
        this.mandatory = true;
        return (T) this;
    }

    /**
     * Mark this key as generated, means it will be automatically created and sent
     * on PDS calls
     */
    @SuppressWarnings("unchecked")
    public T markGenerated() {
        this.generated = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T markSendToPDS() {
        this.sentToPDS = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withDefault(String defaultValue) {
        this.defaultValue = defaultValue;
        return (T) this;
    }

    public T withDefault(boolean booleanDefault) {
        return withDefault("" + booleanDefault);
    }

    @SuppressWarnings("unchecked")
    public T markDefaultRecommended() {
        this.defaultRecommended = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T markForTestingOnly() {
        this.onlyForTesting = true;
        return (T) this;
    }

    public boolean isOnlyForTesting() {
        return onlyForTesting;
    }

    @Override
    public boolean isDefaultRecommended() {
        return defaultRecommended;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + (id != null ? "id=" + id + ", " : "") + "mandatory=" + mandatory + "]";
    }

}