// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

public class AssertInspections{

	private List<Map<String, Object>> inspections;


	AssertInspections() {
		inspections = TestAPI.fetchMetaDataInspections();
	}

	public AssertInspections hasAmountOfInspections(int count) {
		assertEquals(count,inspections.size());
		return this;
	}

	public AssertInspection inspectionNr(int pos) {
		assertTrue(pos<inspections.size());
		return new AssertInspection(inspections.get(pos));
	}

	public class AssertInspection{
		private Map<String, Object> inspection;

		private AssertInspection(Map<String, Object> inspection) {
			this.inspection=inspection;
		}

		public AssertInspection hasId(String id) {
			assertEquals(id,inspection.get("id"));
			return this;
		}

		public AssertInspection hasNotice(String key, String expectedValue) {
			Object data = inspection.get("data");
			assertTrue(data instanceof Map);
			@SuppressWarnings("unchecked")
			Map<String,Object> map = (Map<String, Object>) data;
			Object value = ""+map.get(key);
			assertEquals("For key "+ key+" did not get expected value",expectedValue, value);
			return this;
		}

		public AssertInspections and() {
			return AssertInspections.this;
		}
	}
}