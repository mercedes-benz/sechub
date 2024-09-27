// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page.scan;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mercedesbenz.sechub.webui.RequestConstants;
import com.mercedesbenz.sechub.webui.security.UserInputSanitizer;

@Controller
class ProjectScansController {

    private final UserInputSanitizer sanitizer;

    ProjectScansController(UserInputSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @GetMapping(RequestConstants.PROJECT_SCANS)
    String scans(Model model, @PathVariable("projectId") String projectId) {

        model.addAttribute("scanProjectId", sanitizer.sanitizeProjectId(projectId));

        return "project-scans";
    }
}