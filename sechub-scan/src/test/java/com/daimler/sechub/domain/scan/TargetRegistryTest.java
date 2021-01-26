// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static com.daimler.sechub.domain.scan.TargetType.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class TargetRegistryTest {

	private TargetRegistry registryToTest;

	@Rule
	public ExpectedException expected = ExpectedExceptionFactory.none();
	
	@Before
	public void before() throws Exception {
		registryToTest = new TargetRegistry();
	}
	
	@Test
	public void nothing_registered_each_registry_info_contains_no_target() {
		for (TargetType type: TargetType.values()) {
			TargetRegistryInfo info = registryToTest.createRegistryInfo( type);
			assertFalse(info.containsAtLeastOneTarget());
		}
	}
	
	@Test
	public void codeupload_registered_only_for_this_type_registry_info_contains_a_target() {
		/* prepare */
		Target target1 = mock(Target.class);
		
		when(target1.getType()).thenReturn(TargetType.CODE_UPLOAD);
		when(target1.getIdentifierWithoutPrefix()).thenReturn("test1234");
		
		registryToTest.register(target1);
		
		/* iterate */
		for (TargetType type: TargetType.values()) {
			/* execute*/
			TargetRegistryInfo info = registryToTest.createRegistryInfo( type);

			/* test */
			if (type==TargetType.CODE_UPLOAD) {
				assertTrue(info.containsAtLeastOneTarget());
			}else {
				assertFalse(info.containsAtLeastOneTarget());
			}
		}
	}
	
	@Test
	public void codeupload_registered__registry_info_ontains_justfolderinfo() {
		/* prepare */
		Target target1 = mock(Target.class);
		
		when(target1.getType()).thenReturn(TargetType.CODE_UPLOAD);
		when(target1.getIdentifierWithoutPrefix()).thenReturn("test1234");
		
		Target target2 = mock(Target.class);
		
		when(target2.getType()).thenReturn(TargetType.CODE_UPLOAD);
		when(target2.getIdentifierWithoutPrefix()).thenReturn("test5436");
		
		registryToTest.register(target1);
		registryToTest.register(target2);
		
		/* execute*/
		TargetRegistryInfo info = registryToTest.createRegistryInfo( CODE_UPLOAD);
		
		/* test */
		Set<String> folders = info.getCodeUploadFileSystemFolders();
		assertTrue(folders.contains("test1234"));
		assertTrue(folders.contains("test5436"));
	}


	@Test
	public void registering_a_target_of_type_internet_returns_an_unmodifiable_list_of_targets() {

		/* prepare */
		Target target1 = mock(Target.class);
		when(target1.getType()).thenReturn(INTERNET);
		
		Target target2 = mock(Target.class);
		when(target1.getType()).thenReturn(INTERNET);
		
		/* execute */
		registryToTest.register(target1);

		/* test */
		List<Target> targets = registryToTest.getTargetsFor(INTERNET);
		expected.expect(UnsupportedOperationException.class);// the next line will result in UOE, because unmodifiable
		targets.add(target2);
		
	}

	@Test
	public void registering_a_target_of_type_internet_returns_none_for_intranet_and_target_for_internet() throws Exception {

		/* prepare */
		Target target = mock(Target.class);
		when(target.getType()).thenReturn(INTERNET);
		
		/* execute */
		registryToTest.register(target);

		/* test */
		assertTrue(registryToTest.getTargetsFor(INTRANET).isEmpty());
		assertFalse(registryToTest.getTargetsFor(INTERNET).isEmpty());
		assertTrue(registryToTest.getTargetsFor(INTERNET).contains(target));
		
	}
	
	@Test
	public void registering_a_target_of_type_intranet_returns_none_for_internet_and_target_for_inranet() throws Exception {

		/* prepare */
		Target target = mock(Target.class);
		when(target.getType()).thenReturn(INTRANET);
		
		/* execute */
		registryToTest.register(target);

		/* test */
		assertTrue(registryToTest.getTargetsFor(INTERNET).isEmpty());
		assertFalse(registryToTest.getTargetsFor(INTRANET).isEmpty());
		assertTrue(registryToTest.getTargetsFor(INTRANET).contains(target));
		
	}

	@Test
	public void nothing_registered_get_targets_for_any_type_returns_not_null_but_empty_list() {
		for (TargetType type: TargetType.values()) {
			List<Target> targetsFor = registryToTest.getTargetsFor(type);
			assertNotNull("Type "+type+" results in null ",targetsFor);
			assertTrue("Type "+type+" is not empty",targetsFor.isEmpty());
		}
	}
	
	@Test
	public void nothing_registered_get_targets_for_null_throws_illegal_argument_exception() {
		/* prepare test */
		expected.expect(IllegalArgumentException.class);
		
		/*  execute */
		registryToTest.getTargetsFor(null);
	}
	
	@Test
	public void registering_null_target_throws_illegal_argument_exception() {
		/* prepare test */
		expected.expect(IllegalArgumentException.class);
		
		/*  execute */
		registryToTest.register(null);
	}

}
