// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.developertools.admin.ui.DeveloperAdministrationUI;

@Component
public class DAUICLIRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        DeveloperAdministrationUI.main(args);
    }

}
