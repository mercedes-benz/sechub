package com.mercedesbenz.sechub.webui.page.project;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.webui.page.user.UserInfoService;

@Service
public class ProjectInfoService {

    @Autowired
    UserInfoService userInfoService;
    
    public List<String> fetchProjectIdsForUser(String userId) {
        /* @formatter:off */
        
        List<String> fakeList = new ArrayList<>();
        
        fakeList.add("project-Testval1");
        fakeList.add("project-Testval2");
        fakeList.add("project-Testval3");
        
        return fakeList;
        /* @formatter:on */
    }

    public List<String> fetchProjectIdsForCurrentUser() {
        return fetchProjectIdsForUser(userInfoService.getUserId());
    }
}
