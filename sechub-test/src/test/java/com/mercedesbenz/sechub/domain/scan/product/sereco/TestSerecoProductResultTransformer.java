// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import org.mockito.Mockito;

public class TestSerecoProductResultTransformer extends SerecoProductResultTransformer {

    public TestSerecoProductResultTransformer() {
        this.falsePositiveMarker = Mockito.mock(SerecoFalsePositiveMarker.class);
    }
}
