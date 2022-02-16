// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

/**
 * Sereco is a short name for "Sechub Report Collector". This is the main class
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class Sereco {

    @Lookup
    // create always new instances by container
    // ----------------------------------------
    // dony by workspace is component with scope prototype
    // lookup will let the creation be at container side
    // will use constructor with string argument...
    //
    // also see https://www.baeldung.com/spring-inject-prototype-bean-into-singleton
    public Workspace createWorkspace(String projectId) {
        return null;
    }

}
