// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.monitoring;

import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.test.TestFileSupport;

class PDSClusterMemberTest {

    @Test
    void cluster_member_json_file_can_deserialized() {
        File file = new File("./src/test/resources/cluster/cluster-member-serialized.json");
        String json = TestFileSupport.loadTextFile(file, "\n");
        
        PDSClusterMember member = PDSClusterMember.fromJSON(json);
        assertNotNull(member,"member json import failed!");
    }


}
