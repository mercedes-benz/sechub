// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.cluster;

import static java.lang.System.*;

/**
 * POD information. Uses environment entries provided by downward api.<br>
 * <br>
 * <b>This must be exposed in kubernetes templates!</b><br>
 * <br>
 * See <a href=
 * "https://kubernetes.io/docs/tasks/inject-data-application/downward-api-volume-expose-pod-information">downward
 * api documentation</a> or <a href=
 * "https://stackoverflow.com/questions/37253068/programmatically-get-the-name-of-the-pod-that-a-container-belongs-to-in-kubernet">Stackoverflow
 * entry</a> for more information
 *
 * @author Albert Tregnaghi
 *
 */
public class POD {

    private String name;
    private String namespace;
    private String podInfo;

    public POD() {
        this(getenv("SECHUB_CLUSTER_POD_NAME"), getenv("SECHUB_CLUSTER_POD_NAMESPACE"));
    }

    POD(String name, String namespace) {
        this.name = dropPODMetaInfo(name);
        this.namespace = dropPODMetaInfo(namespace);
        createPodInfo();
    }

    private void createPodInfo() {
        podInfo = "POD:name=" + name + ",namespace=" + namespace;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public String toString() {
        return podInfo;
    }

    public String dropPODMetaInfo(String data) {
        if (data == null) {
            return null;
        }
        int index = data.indexOf('(');
        if (index == -1) {
            return data;
        }
        return data.substring(0, index).trim();
    }
}
