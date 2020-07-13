package com.daimler.sechub.pds.config;

import java.util.ArrayList;
import java.util.List;

public class PDSProdutParameterSetup {

    private List<PDSProdutParameterDefinition> mandatory = new ArrayList<>();
    private List<PDSProdutParameterDefinition> optional = new ArrayList<>();

    public List<PDSProdutParameterDefinition> getMandatory() {
        return mandatory;
    }

    public List<PDSProdutParameterDefinition> getOptional() {
        return optional;
    }

    public void setMandatory(List<PDSProdutParameterDefinition> mandatory) {
        this.mandatory = mandatory;
    }

    public void setOptional(List<PDSProdutParameterDefinition> optional) {
        this.optional = optional;
    }
}
