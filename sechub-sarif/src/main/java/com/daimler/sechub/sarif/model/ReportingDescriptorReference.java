package com.daimler.sechub.sarif.model;

// https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317862
public class ReportingDescriptorReference {

    private String id;
    private String guid;
    private ToolComponentReference toolComponent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317869
     * @return toolcomponent reference
     */
    public ToolComponentReference getToolComponent() {
        return toolComponent;
    }

    public void setToolComponent(ToolComponentReference toolComponent) {
        this.toolComponent = toolComponent;
    }

}
