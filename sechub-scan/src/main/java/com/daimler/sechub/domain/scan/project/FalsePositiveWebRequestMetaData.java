// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.Objects;

public class FalsePositiveWebRequestMetaData {
    
    public static final String PROPERTY_METHOD= "method";
    public static final String PROPERTY_TARGET_URL= "targetUrl";
    public static final String PROPERTY_PROTOCOL= "protocol";
    public static final String PROPERTY_VERSION= "version";
    public static final String PROPERTY_ATTACK_VECTOR= "attackVector";
    
    private String method;
    private String target;
    
    private String protocol;
    private String version;
    
    private String attackVector;

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAttackVector() {
        return attackVector;
    }

    public void setAttackVector(String attackVector) {
        this.attackVector = attackVector;
    }

    @Override
    public String toString() {
        return "FalsePositiveWebRequestMetaData [" + (method != null ? "method=" + method + ", " : "")
                + (target != null ? "targetUrl=" + target + ", " : "") + (protocol != null ? "protocol=" + protocol + ", " : "")
                + (version != null ? "version=" + version + ", " : "") + (attackVector != null ? "attackVector=" + attackVector : "") + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(attackVector, method, protocol, target, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FalsePositiveWebRequestMetaData other = (FalsePositiveWebRequestMetaData) obj;
        return Objects.equals(attackVector, other.attackVector) && Objects.equals(method, other.method) && Objects.equals(protocol, other.protocol)
                && Objects.equals(target, other.target) && Objects.equals(version, other.version);
    }

}
