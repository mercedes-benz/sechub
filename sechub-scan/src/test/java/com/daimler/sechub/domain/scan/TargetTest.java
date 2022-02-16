// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;

import org.junit.Test;

public class TargetTest {

    @Test
    public void new_target_with_identifier__id_without_prefix_adds_automatically_prefix() {
        /* execute */
        Target target = new Target("id1", TargetType.CODE_UPLOAD);

        /* test */
        assertEquals("code_upload://id1", target.getIdentifier());
        assertEquals("id1", target.getIdentifierWithoutPrefix());
    }

    @Test
    public void new_target_with_identifier__id_with_prefix_adds_no_additional_prefix() {
        /* execute */
        Target target = new Target("id1", TargetType.CODE_UPLOAD);

        /* test */
        assertEquals("code_upload://id1", target.getIdentifier());
        assertEquals("id1", target.getIdentifierWithoutPrefix());
    }

}
