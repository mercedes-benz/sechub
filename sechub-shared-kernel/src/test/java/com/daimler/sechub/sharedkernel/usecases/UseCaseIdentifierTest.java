// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class UseCaseIdentifierTest {

    @Test
    public void usecase_numbers_are_unique() {
        for (UseCaseIdentifier identifier: UseCaseIdentifier.values()){
            searchForDuplicates(identifier);
        }
    }

    @Test
    public void usecase_numbers_are_without_fragmentation() {
        List<String> list = new ArrayList<>();
        for (UseCaseIdentifier identifier: UseCaseIdentifier.values()){
            list.add(identifier.uniqueId());
        }
        for (int i=0;i<list.size();i++) {
            int nr = i+1;
            String toSearch = UseCaseIdentifier.createUseCaseID(nr);
            if (! list.contains(toSearch)){
                fail("Expected usecase "+toSearch+" was not found! Seems to be there is a fragmentation!");
            }
                    
        }
    }
    
    private void searchForDuplicates(UseCaseIdentifier source) {
        for (UseCaseIdentifier identifier: UseCaseIdentifier.values()){
            if (source==identifier) {
                continue;
            }
            /* not same so check id */
            if (source.uniqueId().equalsIgnoreCase(identifier.uniqueId())) {
                fail("Found duplicates:\n"+source.name()+" and\n"+identifier.name()+"\n  have both same identifier:"+identifier.uniqueId());
            }
        }
    }

}
