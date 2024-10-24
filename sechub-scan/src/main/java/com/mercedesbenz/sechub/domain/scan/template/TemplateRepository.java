// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TemplateRepository extends JpaRepository<Template, String> {

    @Query(Template.QUERY_All_TEMPLATE_IDS)
    List<String> findAllTemplateIds();
}
