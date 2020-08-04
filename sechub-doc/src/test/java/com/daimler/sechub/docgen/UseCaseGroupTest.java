// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen;

import static org.junit.Assert.*;

import org.junit.Test;

import com.daimler.sechub.docgen.usecase.UseCaseModel;
import com.daimler.sechub.docgen.usecase.UseCaseModel.UseCaseDefGroup;
import com.daimler.sechub.docgen.usecase.UseCaseModel.UseCaseModelType;

public class UseCaseGroupTest {

    @Test
    public void pds_and_sechub_group_with_same_id_are_not_equal() {
        
        /* prepare */
        UseCaseModel model = new UseCaseModel("abc", UseCaseModelType.PDS);
        UseCaseDefGroup group1 = model.new UseCaseDefGroup(UseCaseModelType.PDS);
        group1.name="name1";
        
        UseCaseDefGroup group2 = model.new UseCaseDefGroup(UseCaseModelType.SECHUB);
        group2.name="name1";
        
        /* execute + test */
        assertFalse(group1.equals(group2));
    }
    
    @Test
    public void two_pds_group_instances_having_same_name_and_same_type_are_equal() {
        /* prepare */
        UseCaseModel model = new UseCaseModel("abc", UseCaseModelType.PDS);
        UseCaseDefGroup group1 = model.new UseCaseDefGroup(UseCaseModelType.PDS);
        group1.name="name1";
        
        UseCaseDefGroup group2 = model.new UseCaseDefGroup(UseCaseModelType.PDS);
        group2.name="name1";
        
        /* execute + test */
        assertTrue(group1.equals(group2));
    }
    

}
