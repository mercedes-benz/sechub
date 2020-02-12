package com.daimler.sechub.sharedkernel.mapping;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MappingIdentifierTest {

    @Test
    public void no_duplicated_ids() {
        List<String> list = new ArrayList<>();
        for (MappingIdentifier identifier: MappingIdentifier.values()) {
            String id = identifier.getId();
            if(list.contains(id)) {
                fail("found duplicated mapping identifier:"+id + " inside "+MappingIdentifier.class.getSimpleName()+"."+identifier.name());
            }
            list.add(id);
        }
    }

}
