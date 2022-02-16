// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import com.daimler.sechub.test.SechubTestComponent;
import com.daimler.sechub.test.TestFileSupport;

@SechubTestComponent
public class ScanProductSerecoTestFileSupport extends TestFileSupport {
    private static final ScanProductSerecoTestFileSupport TESTFILE_SUPPORT = new ScanProductSerecoTestFileSupport();

    public static TestFileSupport getTestfileSupport() {
        return TESTFILE_SUPPORT;
    }

    ScanProductSerecoTestFileSupport() {
        super("sechub-scan-product-sereco/src/test/resources");
    }

}
