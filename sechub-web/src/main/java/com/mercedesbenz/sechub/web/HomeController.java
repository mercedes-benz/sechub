// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class HomeController {

    @GetMapping(RequestConstants.HOME)
    public String home(Model model) {
        return "home";
    }
}
