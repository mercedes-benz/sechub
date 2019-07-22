// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductResultRepository extends JpaRepository<ProductResult, UUID>, ProductResultRepositoryCustom {

}
