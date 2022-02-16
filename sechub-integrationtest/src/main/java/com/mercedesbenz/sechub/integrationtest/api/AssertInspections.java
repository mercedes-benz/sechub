// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.sharedkernel.metadata.DefaultMetaDataInspector;
import com.mercedesbenz.sechub.sharedkernel.metadata.IntegrationTestMetaDataInspector;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspector;

/**
 * SecHub server provides a {@link MetaDataInspector} which can remember be used
 * to remember calls and paramters. When we started sechub server in integration
 * test mode, it uses {@link IntegrationTestMetaDataInspector} otherwise a
 * {@link DefaultMetaDataInspector} will be used, which does only log call at
 * debug level.<br>
 * <br>
 *
 * This inspections can be very interesting e.g. for adapter calls
 *
 * @author Albert Tregnaghi
 *
 */
public class AssertInspections {

    private List<Map<String, Object>> inspections;

    AssertInspections() {
        inspections = TestAPI.fetchMetaDataInspections();
    }

    public AssertInspections hasAmountOfInspections(int count) {
        assertEquals(count, inspections.size());
        return this;
    }

    public AssertInspection inspectionNr(int pos) {
        assertTrue("Position is too big! pos:" + pos + ", but size:" + inspections.size(), pos < inspections.size());
        return new AssertInspection(inspections.get(pos));
    }

    public class AssertInspection {
        private Map<String, Object> inspection;

        private AssertInspection(Map<String, Object> inspection) {
            this.inspection = inspection;
        }

        public AssertInspection hasId(String id) {
            assertEquals(id, inspection.get("id"));
            return this;
        }

        public AssertInspection hasNotice(String key, String expectedValue) {
            Object data = inspection.get("data");
            assertTrue(data instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            Object value = "" + map.get(key);
            assertEquals("For key " + key + " did not get expected value", expectedValue, value);
            return this;
        }

        public AssertInspections and() {
            return AssertInspections.this;
        }
    }
}