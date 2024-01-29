// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

public abstract class AbstractDefinitionBuilder<T> {

    protected abstract AbstractDefinition resolveDefinition();

    @SuppressWarnings("unchecked")
    public T comment(String comment) {

        resolveDefinition().setComment(comment);

        return (T) this;
    }

}
