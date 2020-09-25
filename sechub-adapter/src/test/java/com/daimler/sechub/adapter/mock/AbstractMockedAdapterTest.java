// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.mock;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AdapterConfig;

public class AbstractMockedAdapterTest {

	private TestAbstractMockedAdapter testAdapter;

	@Before
	public void before() {
		testAdapter = new TestAbstractMockedAdapter();
		
	}
	
	@Test
	public void with_default_fileending_path_is_like_expected_for_given_value_with_xml_ending() {
		assertEquals("/adapter/mockdata/TestAbstractMockedAdapter/v10/valueX.xml",testAdapter.getPathToMockResultFile("valueX"));
	}
	
	@Test
	public void with_custom_fileending_path_is_like_expected_for_given_value_with_custom_ending() {
		/* prepare */
		testAdapter.fileEnding="something";
		
		/* execute + test */
		assertEquals("/adapter/mockdata/TestAbstractMockedAdapter/v10/valueY.something",testAdapter.getPathToMockResultFile("valueY"));
	}
	
	@SuppressWarnings("rawtypes")
	private class TestAbstractMockedAdapter extends AbstractMockedAdapter{

		private String fileEnding;
		
		@Override
		protected String getMockDataFileEnding() {
			if (fileEnding!=null) {
				return fileEnding;
			}
			return super.getMockDataFileEnding();
		}
		@Override
		public int getAdapterVersion() {
			return 10;
		}
        @Override
        protected void executeMockSanityCheck(AdapterConfig config) {
            // not necessary
        }
		
	}
}
