// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductExecutorConfigRepository extends JpaRepository<ProductExecutorConfig, UUID> {


}
