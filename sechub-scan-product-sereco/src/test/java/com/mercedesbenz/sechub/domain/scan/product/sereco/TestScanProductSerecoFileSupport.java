// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import com.mercedesbenz.sechub.test.SechubTestComponent;
import com.mercedesbenz.sechub.test.TestFileSupport;

@SechubTestComponent
public class TestScanProductSerecoFileSupport extends TestFileSupport {
    private static final TestScanProductSerecoFileSupport TESTFILE_SUPPORT = new TestScanProductSerecoFileSupport();

    public static TestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    TestScanProductSerecoFileSupport() {
        super("sechub-scan-product-sereco/src/test/resources");
    }

}
