// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdminCreatesProject;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdminGrantsAdminRightsToUser;
import com.daimler.sechub.sharedkernel.usecases.anonymous.UseCaseAnonymousCheckAlive;

public class RestDocFactoryTest {

    @Test
    public void create_variant_id_for_a_b_c_string_replaces_spaces_by_hyphen() {
        assertEquals("a-b-c", RestDocFactory.createVariantId("a b c"));
    }

    @Test
    public void create_variant_id_for__space_before_a_b_c_string_replaces_spaces_by_hyphen() {
        assertEquals("-a-b-c", RestDocFactory.createVariantId(" a b c"));
    }

    @Test
    public void create_identfier__anonymous_check_alive() {
        /* execute */
        String identifier = RestDocFactory.createIdentifier(UseCaseAnonymousCheckAlive.class);

        /* test */
        assertEquals("anonymousCheckAlive", identifier);
    }

    @Test
    public void create_identfier__administrator_grants_admin_rights_to_user() {
        /* execute */
        String identifier = RestDocFactory.createIdentifier(UseCaseAdminGrantsAdminRightsToUser.class);

        /* test */
        assertEquals("adminGrantsAdminRightsToUser", identifier);
    }

    @Test
    public void create_summary__anonymous_check_alive() {
        /* execute */
        String summary = RestDocFactory.createSummary(UseCaseAnonymousCheckAlive.class);

        /* test */
        assertEquals("Check if the server is alive and running.", summary);
    }

    @Test
    public void create_description__anonymous_check_alive() {
        /* execute */
        String description = RestDocFactory.createDescription(UseCaseAnonymousCheckAlive.class);

        /* test */
        assertEquals("An anonymous user or system wants to know if the server is alive and running.", description);
    }

    @Test
    public void create_description__administrator_creates_project() {
        /* execute */
        String description = RestDocFactory.createDescription(UseCaseAdminCreatesProject.class);

        /* test */
        assertEquals("Admin creates a project", description);
    }

    @Test
    public void extract_tag__from_url() {
        /* prepare */
        String apiEndpoint = "https://localhost/api/project/{userId}/abc";

        /* execute */
        String tag = RestDocFactory.extractTag(apiEndpoint);

        /* test */
        assertEquals("project", tag);
    }

    @Test
    public void extract_tag__from_url_with_dash() {
        /* prepare */
        String apiEndpoint = "https://localhost/api/project-abc/{userId}/abc";

        /* execute */
        String tag = RestDocFactory.extractTag(apiEndpoint);

        /* test */
        assertEquals("project-abc", tag);
    }

    @Test
    public void extract_tag__from_url_with_dash_api() {
        /* prepare */
        String apiEndpoint = "https://localhost/api/project-api/{userId}/abc";

        /* execute */
        String tag = RestDocFactory.extractTag(apiEndpoint);

        /* test */
        assertEquals("project-api", tag);
    }

    @Test
    public void extract_tag__from_url_with_underscore() {
        /* prepare */
        String apiEndpoint = "https://localhost/api/project_mapping/{userId}/abc";

        /* execute */
        String tag = RestDocFactory.extractTag(apiEndpoint);

        /* test */
        assertEquals("project_mapping", tag);
    }
}
