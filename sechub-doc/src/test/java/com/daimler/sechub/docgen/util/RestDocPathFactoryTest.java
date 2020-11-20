// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.daimler.sechub.sharedkernel.usecases.anonymous.UseCaseAnonymousCheckAlive;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserCreatesNewJob;

public class RestDocPathFactoryTest {

	@Test
	public void create_variant_id_for_a_b_c_string_replaces_spaces_by_hyphen() {
		assertEquals("a-b-c",RestDocPathFactory.createVariantId("a b c"));
	}

	@Test
	public void create_variant_id_for__space_before_a_b_c_string_replaces_spaces_by_hyphen() {
		assertEquals("-a-b-c",RestDocPathFactory.createVariantId(" a b c"));
	}

	@Test
	public void create_identfier__anonymous_check_alive() {      
        /* execute */
        String identifier = RestDocPathFactory.createIdentifier(UseCaseAnonymousCheckAlive.class);
        
        /* test */
	    assertEquals("anonymousCheckAlive", identifier);
	}
    
    @Test
    public void create_path__anonymous_check_alive() {      
        /* execute */
        String path = RestDocPathFactory.createPath(UseCaseAnonymousCheckAlive.class, "Get");
        
        /* test */
        assertEquals("anonymousCheckAlive_get", path);
    }
    
    @Test
    public void create_path__user_creates_new_job() {      
        /* execute */
        String path = RestDocPathFactory.createPath(UseCaseUserCreatesNewJob.class, "Code Scan");
        
        /* test */
        assertEquals("userCreatesNewJob_code-scan", path);
    }
}
