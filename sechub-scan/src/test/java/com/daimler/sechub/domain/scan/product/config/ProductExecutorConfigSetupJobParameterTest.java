// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ProductExecutorConfigSetupJobParameterTest {

    @Test
    public void two_parameters_with_same_id_are_equal_even_when_values_are_different() {
        /* prepare */
        ProductExecutorConfigSetupJobParameter p1 = new ProductExecutorConfigSetupJobParameter("test.key1","v1");
        ProductExecutorConfigSetupJobParameter p2 = new ProductExecutorConfigSetupJobParameter("test.key1","v2");
        
        /* execute + test*/
        assertEquals(p1,p2);
    }
    
    @Test
    public void a_parameter_added_to_list_is_found_by_contains_with_other_param_instance_but_same_id() {
        /* prepare */
        ProductExecutorConfigSetupJobParameter p1 = new ProductExecutorConfigSetupJobParameter("test.key1","v1");
        ProductExecutorConfigSetupJobParameter p2 = new ProductExecutorConfigSetupJobParameter("test.key1","v2");

        List<ProductExecutorConfigSetupJobParameter> list = new ArrayList<>();
        list.add(p1);
        
        /* execute + test*/
        assertTrue(list.contains(p1));
        assertTrue(list.contains(p2));
    }

}
