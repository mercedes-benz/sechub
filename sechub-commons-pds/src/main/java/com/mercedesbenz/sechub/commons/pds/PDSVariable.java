package com.mercedesbenz.sechub.commons.pds;

public interface PDSVariable {

    /**
     * Returns the variable id. This is always in a form like "a.b.c" even when
     * variable is of type {@link PDSVariableType#ENVIRONMENT_VARIABLE}
     *
     * @return the key
     */
    public String getVariableId();

    public PDSVariableType getVariableType();
}
