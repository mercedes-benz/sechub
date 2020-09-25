// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This message data object contains all possible information about a spring
 * batch job
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between domains about cluster member changes")
public class ClusterMemberMessage implements JSONable<ClusterMemberMessage> {

    private String hostName;

    private String serviceStatus;
    
    private String serviceName;
    
    private String information;

    @Override
    public Class<ClusterMemberMessage> getJSONTargetClass() {
        return ClusterMemberMessage.class;
    }

    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String name) {
        this.serviceName = name;
    }
    
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostname) {
        this.hostName = hostname;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String status) {
        this.serviceStatus = status;
    }
    public String getInformation() {
        return information;
    }
    
    public void setInformation(String information) {
        this.information = information;
    }
    
}
