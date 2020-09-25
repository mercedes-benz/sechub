// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.pds.config.PDSScanType;

/**
 * PDS is complete standalone application - no sechub dependencies. here we check that scan type
 * is compatible - so when e.g. a "containerScan" is added for sechub the test will fail and we know
 * we have forgotten to implement in PDS...
 * @author Albert Tregnaghi
 *
 */
public class ScanTypeTest {

    @Test
    public void scantypes_sechub_ids_are_all_found_in_pds_scantypes() {
        /* test */

        // check every sechub scan type is recognized in pds
        for (ScanType scantype : ScanType.values()) {
            PDSScanType pdsSanType = PDSScanType.valueOf(scantype.name());
            assertNotNull("Should not happen - because of valueOf should throw exception in this case", pdsSanType);
            assertEquals("scan type ids differ!", scantype.getId(),pdsSanType.getId());
        }
    }
    
    @Test
    public void scantypes_pds_ids_are_all_found_in_sechub_scantypes() {
        /* test */

        // check every pds scan type is recognized in sechub
        for (PDSScanType scantype : PDSScanType.values()) {
            ScanType scanType = ScanType.valueOf(scantype.name());
            assertNotNull("Should not happen - because of valueOf should throw exception in this case", scanType);
            assertEquals("scan type ids differ!", scantype.getId(),scanType.getId());
        }
    }
}
