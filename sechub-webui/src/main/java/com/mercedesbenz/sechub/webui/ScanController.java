// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScanController {
    @GetMapping("/projects/{projectId}/scans")
    String scans(Model model) {
        return "scans";
    }
}
