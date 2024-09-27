// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page.project;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.webui.page.user.UserInfoService;

@Service
class ProjectInfoService {

    private final UserInfoService userInfoService;

    ProjectInfoService(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    List<String> fetchProjectIdsForUser(String userId) {
        /* @formatter:off */

        List<String> fakeList = new ArrayList<>();

        fakeList.add("project-Testval1");
        fakeList.add("project-Testval2");
        fakeList.add("project-Testval3");

        return fakeList;
        /* @formatter:on */
    }

    List<String> fetchProjectIdsForCurrentUser() {
        return fetchProjectIdsForUser(userInfoService.getUserId());
    }
}
