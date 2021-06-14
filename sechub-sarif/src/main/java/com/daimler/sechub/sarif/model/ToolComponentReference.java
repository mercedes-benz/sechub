package com.daimler.sechub.sarif.model;

/**
 * https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317875
 * @author Albert Tregnaghi
 *
 */
public class ToolComponentReference {

    private String name;
    private String guid;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getGuid() {
        return guid;
    }
}
