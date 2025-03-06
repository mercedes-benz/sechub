// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = { Template.class, TemplateRepository.class, TemplateRepositoryDBTest.SimpleTestConfiguration.class })
class TemplateRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TemplateRepository repositoryToTest;

    @Test
    void findAllTemplateIds_no_assets_exist() throws Exception {
        /* execute */
        List<String> result = repositoryToTest.findAllTemplateIds();

        /* test */
        assertThat(result).isEmpty();
    }

    @Test
    void findAllTemplateIds() throws Exception {
        /* prepare */
        Template template1 = new Template("template1");
        template1.setDefinition("tempalte1-definition");
        entityManager.persist(template1);

        Template template2 = new Template("template2");
        template2.setDefinition("tempalte2-definition");
        entityManager.persist(template2);

        /* execute */
        List<String> result = repositoryToTest.findAllTemplateIds();

        /* test */
        assertThat(result).contains("template1", "template2").hasSize(2);
    }

    @Test
    void deleteTemplateById_no_assets_exist() throws Exception {
        /* execute */
        int numberOfDeletedEntries = repositoryToTest.deleteTemplateById("no-existing-template");

        /* test */
        assertEquals(0, numberOfDeletedEntries);
    }

    @Test
    void deleteTemplateById() throws Exception {
        /* prepare */
        Template templateA = new Template("templateA");
        templateA.setDefinition("tempalteA-definition");
        entityManager.persist(templateA);

        Template templateB = new Template("templateB");
        templateB.setDefinition("tempalteB-definition");
        entityManager.persist(templateB);

        /* execute */
        int numberOfDeletedEntries = repositoryToTest.deleteTemplateById(templateB.getId());

        /* test */
        assertEquals(1, numberOfDeletedEntries);
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}