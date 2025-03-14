// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import static com.mercedesbenz.sechub.domain.scan.template.Template.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemplateRepository extends JpaRepository<Template, String> {

    @Query(Template.QUERY_All_TEMPLATE_IDS)
    List<String> findAllTemplateIds();

    @Modifying
    @Query(value = "DELETE from " + TABLE_NAME + " WHERE " + COLUMN_TEMPLATE_ID + "=:templateId", nativeQuery = true)
    int deleteTemplateById(@Param("templateId") String templateId);
}
