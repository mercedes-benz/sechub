package com.daimler.sechub.sarif.model;

/**
 * A base class for all sarif objects mentioned inside the SARIF documentation.
 * Provides some base parts available in every object. E.g. "properties" as
 * described at
 * https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317448
 * 
 * @author Albert Tregnaghi
 *
 */
class SarifObject {

    private PropertyBag properties;

    /**
     * @return property bag or <code>null</code>
     */
    public PropertyBag getProperties() {
        return properties;
    }

    public void setProperties(PropertyBag properties) {
        this.properties = properties;
    }

}
