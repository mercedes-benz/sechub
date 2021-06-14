package com.daimler.sechub.sarif.model;

/**
 * https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317870
 * @author Albert Tregnaghi
 *
 */
public class ReportingDescriptorRelationship  {

    private ReportingDescriptorReference target;
    
    public ReportingDescriptorReference getTarget() {
        return target;
    }
    
    public void setTarget(ReportingDescriptorReference target) {
        this.target = target;
    }
    
    
}
