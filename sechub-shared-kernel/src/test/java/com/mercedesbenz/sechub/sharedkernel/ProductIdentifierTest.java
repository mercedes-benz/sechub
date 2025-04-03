// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.commons.model.ScanType;

class ProductIdentifierTest {

    @ParameterizedTest()
    @EnumSource(ProductIdentifier.class)
    @NullSource
    void fromString__enum_value(ProductIdentifier productIdentifier) {
        /* prepare */
        ProductIdentifier expected = productIdentifier == null ? ProductIdentifier.UNKNOWN : productIdentifier;
        String productIdName = productIdentifier == null ? null : productIdentifier.name();

        /* execute */
        ProductIdentifier result = ProductIdentifier.fromString(productIdName);

        /* test */
        assertEquals(expected, result);
    }

    @ParameterizedTest()
    @EnumSource(ProductIdentifier.class)
    void fromString__lower_case(ProductIdentifier productIdentifier) {
        /* prepare */
        ProductIdentifier expected = productIdentifier;
        String productIdName = productIdentifier.name().toLowerCase();

        /* execute */
        ProductIdentifier result = ProductIdentifier.fromString(productIdName);

        /* test */
        assertEquals(expected, result);
    }

    @Test
    void fromString__fantasy_type() {
        /* prepare */
        ProductIdentifier expected = ProductIdentifier.UNKNOWN;
        String productIdName = "fantasyProduct";

        /* execute */
        ProductIdentifier result = ProductIdentifier.fromString(productIdName);

        /* test */
        assertEquals(expected, result);
    }

    @ParameterizedTest()
    @ArgumentsSource(ExpectedScanTypesForProductIdentifierArgumentsProvider.class)
    void productIdentifier_has_expected_scantype(ProductIdentifier productIdentifier, ScanType expectedScanType) {
        /* prepare */

        /* execute */
        ScanType result = productIdentifier.getType();

        /* test */
        assertEquals(expectedScanType, result);
    }

    private static class ExpectedScanTypesForProductIdentifierArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              Arguments.of(ProductIdentifier.CHECKMARX      ,ScanType.CODE_SCAN   ),

              Arguments.of(ProductIdentifier.PDS_CODESCAN   ,ScanType.CODE_SCAN   ),
              Arguments.of(ProductIdentifier.PDS_SECRETSCAN ,ScanType.SECRET_SCAN ),
              Arguments.of(ProductIdentifier.PDS_IACSCAN    ,ScanType.IAC_SCAN    ) ,
              Arguments.of(ProductIdentifier.PDS_LICENSESCAN,ScanType.LICENSE_SCAN),

              Arguments.of(ProductIdentifier.PDS_WEBSCAN    ,ScanType.WEB_SCAN    ),

              Arguments.of(ProductIdentifier.PDS_ANALYTICS  ,ScanType.ANALYTICS   ),
              Arguments.of(ProductIdentifier.PDS_PREPARE    ,ScanType.PREPARE     )
              );
        }
        /* @formatter:on*/
    }
}
