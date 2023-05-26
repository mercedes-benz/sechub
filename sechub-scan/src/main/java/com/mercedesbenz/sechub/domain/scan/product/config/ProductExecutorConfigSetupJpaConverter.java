// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import jakarta.persistence.AttributeConverter;

public class ProductExecutorConfigSetupJpaConverter implements AttributeConverter<ProductExecutorConfigSetup, String> {

    @Override
    public String convertToDatabaseColumn(ProductExecutorConfigSetup attribute) {
        return attribute.toJSON();
    }

    @Override
    public ProductExecutorConfigSetup convertToEntityAttribute(String dbData) {
        return ProductExecutorConfigSetup.fromJSONString(dbData);
    }

}
